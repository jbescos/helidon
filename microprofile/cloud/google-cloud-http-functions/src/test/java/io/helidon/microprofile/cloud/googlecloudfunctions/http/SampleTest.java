package io.helidon.microprofile.cloud.googlecloudfunctions.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

public class SampleTest implements HttpFunction {

    @Inject
    private MyService myService;

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        String value = request.getReader().readLine();
        response.getWriter().write(myService.toUpperCase(value));
    }

    @Test
    public void example() throws IOException, InterruptedException {
        try (LocalServerTestSupport.ServerProcess process = LocalServerTestSupport.startServer(GoogleCloudHttpFunction.class, "http")) {
            Response response = ClientBuilder.newClient().target("http://localhost:8080/").request().post(Entity.json("test"));
            assertEquals("TEST", response.readEntity(String.class));
            assertEquals(200, response.getStatus());
            response = ClientBuilder.newClient().target("http://localhost:8080/").request().post(Entity.json("test2"));
            assertEquals("TEST2", response.readEntity(String.class));
            assertEquals(200, response.getStatus());
        }
    }

    @ApplicationScoped
    public static class MyService {

        public String toUpperCase(String str) {
            return str.toUpperCase();
        }

    }

}
