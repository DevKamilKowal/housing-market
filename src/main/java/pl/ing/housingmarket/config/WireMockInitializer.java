package pl.ing.housingmarket.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@Component
@RequiredArgsConstructor
public class WireMockInitializer {
    private final WireMockServer wireMockServer;

    @PostConstruct
    public void init() {
        wireMockServer.stubFor(get(urlPathMatching("/api/real-estates/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("response-template.json")));
    }

}
