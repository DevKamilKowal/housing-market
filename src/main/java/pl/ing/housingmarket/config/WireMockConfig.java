package pl.ing.housingmarket.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WireMockConfig {
    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockHouseRegistryService() {
        return new WireMockServer(WireMockConfiguration.options()
                .port(8081)
                .extensions(new ResponseTemplateTransformer(true))
                .withRootDirectory("src/main/resources"));
    }
}
