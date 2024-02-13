package pl.ing.housingmarket.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import pl.ing.housingmarket.model.Region;
import pl.ing.housingmarket.model.response.HouseResponse;

@FeignClient(name = "house-registry-client", url = "${house.registry.endpoint.url}")
public interface HouseRegistryClient {
    @GetMapping("/api/real-estates/{regionID}")
    HouseResponse getHouses(@PathVariable("regionID") Region regionID, @RequestParam(defaultValue = "1") int pageNo);
}
