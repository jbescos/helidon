
/*
 * Copyright (c) 2018, 2022 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.helidon.security.examples.signatures;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.helidon.common.configurable.Resource;
import io.helidon.common.http.HttpMediaType;
import io.helidon.common.pki.KeyConfig;
import io.helidon.reactive.webserver.Routing;
import io.helidon.reactive.webserver.WebServer;
import io.helidon.security.CompositeProviderFlag;
import io.helidon.security.CompositeProviderSelectionPolicy;
import io.helidon.security.Security;
import io.helidon.security.SecurityContext;
import io.helidon.security.Subject;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.security.providers.common.OutboundConfig;
import io.helidon.security.providers.common.OutboundTarget;
import io.helidon.security.providers.httpauth.HttpBasicAuthProvider;
import io.helidon.security.providers.httpauth.SecureUserStore;
import io.helidon.security.providers.httpsign.HttpSignProvider;
import io.helidon.security.providers.httpsign.InboundClientDefinition;
import io.helidon.security.providers.httpsign.OutboundTargetDefinition;

/**
 * Example of authentication of service with http signatures, using configuration file as much as possible.
 */
public class SignatureExampleBuilderMain {
    private static final Map<String, SecureUserStore.User> USERS = new HashMap<>();
    // used from unit tests
    private static WebServer service1Server;
    private static WebServer service2Server;

    static {
        addUser("jack", "password", List.of("user", "admin"));
        addUser("jill", "password", List.of("user"));
        addUser("john", "password", List.of());
    }

    private SignatureExampleBuilderMain() {
    }

    public static WebServer getService1Server() {
        return service1Server;
    }

    public static WebServer getService2Server() {
        return service2Server;
    }

    private static void addUser(String user, String password, List<String> roles) {
        USERS.put(user, new SecureUserStore.User() {
            @Override
            public String login() {
                return user;
            }

            char[] password() {
                return password.toCharArray();
            }

            @Override
            public boolean isPasswordValid(char[] password) {
                return Arrays.equals(password(), password);
            }

            @Override
            public Collection<String> roles() {
                return roles;
            }
        });
    }

    /**
     * Starts this example.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        // to allow us to set host header explicitly
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        // start service 2 first, as it is required by service 1
        service2Server = SignatureExampleUtil.startServer(routing2(), 9080);
        service1Server = SignatureExampleUtil.startServer(routing1(), 8080);

        System.out.println("Signature example: from builder");
        System.out.println();
        System.out.println("Users:");
        System.out.println("jack/password in roles: user, admin");
        System.out.println("jill/password in roles: user");
        System.out.println("john/password in no roles");
        System.out.println();
        System.out.println("***********************");
        System.out.println("** Endpoints:        **");
        System.out.println("***********************");
        System.out.println("Basic authentication, user role required, will use symmetric signatures for outbound:");
        System.out.printf("  http://localhost:%1$d/service1%n", service1Server.port());
        System.out.println("Basic authentication, user role required, will use asymmetric signatures for outbound:");
        System.out.printf("  http://localhost:%1$d/service1-rsa%n", service2Server.port());
        System.out.println();
    }

    private static Routing routing2() {

        // build routing (security is loaded from config)
        return Routing.builder()
                // helper method to load both security and web server security from configuration
                .register(WebSecurity.create(security2()).securityDefaults(WebSecurity.authenticate()))
                .get("/service2", WebSecurity.rolesAllowed("user"))
                .get("/service2-rsa", WebSecurity.rolesAllowed("user"))
                // web server does not (yet) have possibility to configure routes in config files, so explicit...
                .get("/{*}", (req, res) -> {
                    Optional<SecurityContext> securityContext = req.context().get(SecurityContext.class);
                    res.headers().contentType(HttpMediaType.PLAINTEXT_UTF_8);
                    res.send("Response from service2, you are: \n" + securityContext
                            .flatMap(SecurityContext::user)
                            .map(Subject::toString)
                            .orElse("Security context is null") + ", service: " + securityContext
                            .flatMap(SecurityContext::service)
                            .map(Subject::toString));
                })
                .build();
    }

    private static Routing routing1() {
        // build routing (security is loaded from config)
        return Routing.builder()
                .register(WebSecurity.create(security1()).securityDefaults(WebSecurity.authenticate()))
                .get("/service1",
                     WebSecurity.rolesAllowed("user"),
                     (req, res) -> SignatureExampleUtil.processService1Request(req, res, "/service2", service2Server.port()))
                .get("/service1-rsa",
                     WebSecurity.rolesAllowed("user"),
                     (req, res) -> SignatureExampleUtil.processService1Request(req, res, "/service2-rsa", service2Server.port()))
                .build();
    }

    private static Security security2() {
        return Security.builder()
                .providerSelectionPolicy(CompositeProviderSelectionPolicy.builder()
                                                 .addAuthenticationProvider("http-signatures", CompositeProviderFlag.OPTIONAL)
                                                 .addAuthenticationProvider("basic-auth")
                                                 .build())
                .addProvider(HttpBasicAuthProvider.builder()
                                     .realm("mic")
                                     .userStore(users()),
                             "basic-auth")
                .addProvider(HttpSignProvider.builder()
                                     .addInbound(InboundClientDefinition.builder("service1-hmac")
                                                         .principalName("Service1 - HMAC signature")
                                                         .hmacSecret("somePasswordForHmacShouldBeEncrypted")
                                                         .build())
                                     .addInbound(InboundClientDefinition.builder("service1-rsa")
                                                         .principalName("Service1 - RSA signature")
                                                         .publicKeyConfig(KeyConfig.keystoreBuilder()
                                                                                  .keystore(Resource.create(Paths.get(
                                                                                          "src/main/resources/keystore.p12")))
                                                                                  .keystorePassphrase("password".toCharArray())
                                                                                  .certAlias("service_cert")
                                                                                  .build())
                                                         .build())
                                     .build(),
                             "http-signatures")
                .build();
    }

    private static Security security1() {
        return Security.builder()
                .providerSelectionPolicy(CompositeProviderSelectionPolicy.builder()
                                                 .addOutboundProvider("basic-auth")
                                                 .addOutboundProvider("http-signatures")
                                                 .build())
                .addProvider(HttpBasicAuthProvider.builder()
                                     .realm("mic")
                                     .userStore(users())
                                     .addOutboundTarget(OutboundTarget.builder("propagate-all").build()),
                             "basic-auth")
                .addProvider(HttpSignProvider.builder()
                                     .outbound(OutboundConfig.builder()
                                                       .addTarget(hmacTarget())
                                                       .addTarget(rsaTarget())
                                                       .build()),
                             "http-signatures")
                .build();
    }

    private static OutboundTarget rsaTarget() {
        return OutboundTarget.builder("service2-rsa")
                .addHost("localhost")
                .addPath("/service2-rsa.*")
                .customObject(OutboundTargetDefinition.class,
                              OutboundTargetDefinition.builder("service1-rsa")
                                      .privateKeyConfig(KeyConfig.keystoreBuilder()
                                                                .keystore(Resource.create(Paths.get(
                                                                        "src/main/resources/keystore.p12")))
                                                                .keystorePassphrase("password".toCharArray())
                                                                .keyAlias("myPrivateKey")
                                                                .build())
                                      .build())
                .build();
    }

    private static OutboundTarget hmacTarget() {
        return OutboundTarget.builder("service2")
                .addHost("localhost")
                .addPath("/service2")
                .customObject(
                        OutboundTargetDefinition.class,
                        OutboundTargetDefinition.builder("service1-hmac")
                                .hmacSecret("somePasswordForHmacShouldBeEncrypted")
                                .build())
                .build();
    }

    private static SecureUserStore users() {
        return login -> Optional.ofNullable(USERS.get(login));
    }
}
