package pl.ing.housingmarket.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;


@TestConfiguration
public class WireMockConfigTests {
    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockHouseRegistryService() {
        return new WireMockServer(WireMockConfiguration.options()
                .port(8082)
                .extensions(new ResponseTemplateTransformer(true)));
    }
}
