/*
 * Copyright (c) 2017, 2022 Oracle and/or its affiliates.
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

package io.helidon.reactive.webserver.examples.staticcontent;

import io.helidon.common.http.Http;
import io.helidon.reactive.media.jsonp.JsonpSupport;
import io.helidon.reactive.webserver.Routing;
import io.helidon.reactive.webserver.WebServer;
import io.helidon.reactive.webserver.staticcontent.StaticContentSupport;

/**
 * Application demonstrates combination of the static content with a simple REST API. It counts accesses and display it
 * on the WEB page.
 */
public class Main {
    private static final Http.HeaderValue UI_REDIRECT = Http.Header.createCached(Http.Header.LOCATION, "/ui");

    private Main() {
    }

    /**
     * Creates new {@link Routing}.
     *
     * @return the new instance
     */
    static Routing createRouting() {
        return Routing.builder()
                .any("/", (req, res) -> {
                    // showing the capability to run on any path, and redirecting from root
                    res.status(Http.Status.MOVED_PERMANENTLY_301);
                    res.headers().set(UI_REDIRECT);
                    res.send();
                })
                .register("/ui", new CounterService())
                .register("/ui", StaticContentSupport.builder("WEB")
                        .welcomeFileName("index.html")
                        .build())
                .build();
    }

    /**
     * A java main class.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        WebServer server = WebServer.builder(createRouting())
                .port(8080)
                .addMediaSupport(JsonpSupport.create())
                .build();

        // Start the server and print some info.
        server.start().thenAccept(ws -> {
            System.out.println("WEB server is up! http://localhost:" + ws.port());
        });

        // Server threads are not demon. NO need to block. Just react.
        server.whenShutdown()
                .thenRun(() -> System.out.println("WEB server is DOWN. Good bye!"));

    }
}
