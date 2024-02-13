package pl.ing.housingmarket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pl.ing.housingmarket.config.WireMockConfigTests;
import pl.ing.housingmarket.model.House;
import pl.ing.housingmarket.model.Region;
import pl.ing.housingmarket.model.response.ErrorResponse;
import pl.ing.housingmarket.repository.HouseRepository;
import pl.ing.housingmarket.service.HouseService;
import pl.ing.housingmarket.service.provider.DateProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@ContextConfiguration(classes = WireMockConfigTests.class)
@EnableFeignClients
class MainTests {
    @Autowired
    private MockMvc postman;

    @MockBean
    private DateProvider dateProvider;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private HouseService houseService;

    @Test
    void shouldInsertAllHousesOnExecutedEvent() {
        LocalDateTime creationTime = LocalDateTime.of(2024, Month.JANUARY, 1, 12, 0, 0, 0);
        Mockito.when(dateProvider.now()).thenReturn(creationTime);

        long allRecordsBeforeFetch = houseRepository.findAll().stream().filter(
                h -> h.getCreationDate().equals(LocalDate.from(creationTime))).count();

        houseService.fetchAllHouses();

        for (Region region : Region.values()) {
            assertEquals(9, houseRepository.findAll().stream().filter(h -> h.getRegion() == region
                    && h.getCreationDate().equals(LocalDate.from(creationTime))).count());
        }

        long allRecordsAfterFetch = houseRepository.findAll().stream().filter(
                h -> h.getCreationDate().equals(LocalDate.from(creationTime))).count();
        //w response-template dla testsa zawsze zwraca per region 3 page po 3 domy
        int expectedRecordsAfterFetch = Region.values().length * 9;

        assertEquals(0L, allRecordsBeforeFetch);
        assertEquals(expectedRecordsAfterFetch, allRecordsAfterFetch);
    }

    @Test
    void shouldCalculateCorrectAveragePrices() throws Exception {
        LocalDateTime creationTime = LocalDateTime.of(2024, Month.JANUARY, 2, 12, 0, 0, 0);
        Mockito.when(dateProvider.now()).thenReturn(creationTime);

        House h1 = new House("flat", new BigDecimal("300000.00"), "Modern urban flat with sleek design.", new BigDecimal("50.0"), 2, Region.SL_POL, LocalDate.from(creationTime));
        House h2 = new House("flat", new BigDecimal("500000.00"), "Contemporary flat in a prime location.", new BigDecimal("55.0"), 2, Region.SL_POL, LocalDate.from(creationTime));
        House h3 = new House("terraced_house", new BigDecimal("400000.00"), "Cozy terraced house with a small garden.", new BigDecimal("75.0"), 3, Region.SL_KATO, LocalDate.from(creationTime));
        House h4 = new House("flat", new BigDecimal("300000.00"), "Spacious flat with modern amenities.", new BigDecimal("60.0"), 3, Region.SL_KATO, LocalDate.from(creationTime));
        List<House> houses = Arrays.asList(h1, h2, h3, h4);

        houseRepository.saveAll(houses);

        postman.perform(get("/api/real-estate-stats/{region}", h1.getRegion())
                        .param("dateSince", "20240102")
                        .param("dateUntil", "20240102")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avgValue").value((h1.getPrice().add(h2.getPrice()).divide(new BigDecimal("2.00"), 1, RoundingMode.UP))));

        postman.perform(get("/api/real-estate-stats/{region}", h3.getRegion())
                        .param("dateSince", "20240102")
                        .param("dateUntil", "20240102")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avgValue").value((h3.getPrice().add(h4.getPrice()).divide(new BigDecimal("2.00"), 1, RoundingMode.UP))));

        postman.perform(get("/api/real-estate-stats/{region}", h3.getRegion())
                        .param("dateSince", "20240102")
                        .param("dateUntil", "20240102")
                        .param("types", "terraced_house")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avgValue").value(h3.getPrice().setScale(1, RoundingMode.UP)));

    }

    @Test
    void shouldNotCalculateCorrectAveragePrices_RegionDoesntExist() throws Exception {
        String region = "gfhhjgfdlhjgfdihbmj fgdi45j6lij sil bterjlibrets4e";
        postman.perform(get("/api/real-estate-stats/{region}", region)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(new ErrorResponse("This region doesn't exist : " + region).message()));
    }

    @Test
    void shouldNotCalculateCorrectAveragePrices_SizeDoesntExist() throws Exception {
        LocalDateTime creationTime = LocalDateTime.of(2024, Month.JANUARY, 3, 12, 0, 0, 0);
        Mockito.when(dateProvider.now()).thenReturn(creationTime);

        House h1 = new House("flat", new BigDecimal("350000.00"), "Modern urban flat.", new BigDecimal("55.0"), 3, Region.SL_KATO, LocalDate.from(creationTime));
        String size = "jlkgfd";

        postman.perform(get("/api/real-estate-stats/{region}", h1.getRegion())
                        .param("dateSince", "20240103")
                        .param("dateUntil", "20240103")
                        .param("size", size)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(new ErrorResponse("This size isn't supported : " + size).message()));
    }


}
