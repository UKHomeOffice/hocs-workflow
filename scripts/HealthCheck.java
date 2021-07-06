package uk.gov.digital.ho.hocs;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Class used for container health check by docker compose
 * Pass address to be health checked as the single command line argument
 */
public class HealthCheck {

    public static void main(String[] args) throws IOException, InterruptedException {

        if(args.length < 1){
            throw new IllegalArgumentException("No URL received as command line argument");
        }

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(args[0]))
                .header("accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 || !response.body().contains("UP")) {
            throw new RuntimeException("Health check to " + args[0] + " failed with status code: " + response.statusCode());
        }
    }

}
