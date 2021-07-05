package uk.gov.digital.ho.hocs;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Class used for container health check by docker compose
 */
public class HealthCheck {

    public static void main(String[] args) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/actuator/health"))
                .header("accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 || !response.body().contains("UP")) {
            throw new RuntimeException("Healthcheck failed with status code: " + response.statusCode());
        }
    }

}
