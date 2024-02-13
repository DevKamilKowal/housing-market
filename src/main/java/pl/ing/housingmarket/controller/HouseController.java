package pl.ing.housingmarket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.ing.housingmarket.model.Region;
import pl.ing.housingmarket.model.response.AverageHouseValueResponse;
import pl.ing.housingmarket.service.HouseService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/real-estate-stats")
@RequiredArgsConstructor
@Slf4j
public class HouseController {
    private final HouseService houseService;

    @GetMapping("/{regionID}")
    public ResponseEntity<AverageHouseValueResponse> getAverageHouseValue(@PathVariable String regionID, @RequestParam(required = false) String size,
                                                                          @RequestParam(required = false) Integer rooms,
                                                                          @RequestParam(required = false) List<String> types,
                                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDate dateSince,
                                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDate dateUntil) {
        log.info(buildLogMessageGetAverageValue(regionID, size, rooms, types, dateSince, dateUntil));
        return ResponseEntity.ok(houseService.getAveragePrice(regionID, size, rooms, types, dateSince, dateUntil));
    }

    private String buildLogMessageGetAverageValue(String regionID, String size, Integer rooms, List<String> types, LocalDate dateSince, LocalDate dateUntil) {
        StringBuilder logMessage = new StringBuilder("getAverageHouseValue called with parameters: ");
        logMessage.append("regionID=").append(regionID);

        if (size != null) logMessage.append(", size=").append(size);
        if (rooms != null) logMessage.append(", rooms=").append(rooms);
        if (types != null && !types.isEmpty()) logMessage.append(", types=").append(types);
        if (dateSince != null) logMessage.append(", dateSince=").append(dateSince);
        if (dateUntil != null) logMessage.append(", dateUntil=").append(dateUntil);

        return logMessage.toString();
    }


}
