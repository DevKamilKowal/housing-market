package pl.ing.housingmarket.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.ing.housingmarket.exception.RegionNotExistsException;
import pl.ing.housingmarket.exception.SizeNotExistsException;
import pl.ing.housingmarket.integration.HouseRegistryClient;
import pl.ing.housingmarket.model.House;
import pl.ing.housingmarket.model.Region;
import pl.ing.housingmarket.model.response.AverageHouseValueResponse;
import pl.ing.housingmarket.model.response.HouseResponse;
import pl.ing.housingmarket.service.provider.DateProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HouseService {
    private final HouseRegistryClient houseRegistryClient;
    private final EmailService emailService;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private final DateProvider dateProvider;
    @Value("${house.fetching.retries}")
    private int maxAttempts;

    @Transactional
    public void fetchAllHouses() {
        long start = System.currentTimeMillis();
        log.info("fetchAllHouses()");
        for (Region region : Region.values()) {
            processRegion(region, 1);
        }
        long end = System.currentTimeMillis();
        log.info("During time : {} ms", end - start);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRegion(Region region, int startingPageNo) {
        int attempts = 0;
        int pageNo = startingPageNo;
        HouseResponse houseResponse = null;

        while (attempts < maxAttempts) {
            try {
                houseResponse = getHouseResponse(region, pageNo);
                if (houseResponse != null && !houseResponse.data().isEmpty()) {
                    insertHousesToDB(houseResponse.data(), region);
                    if (pageNo < houseResponse.totalPages()) {
                        pageNo++;
                    } else {
                        log.info("All houses for {} are fetched", region);
                        break;
                    }
                } else {
                    attempts++;
                    log.warn("No data for region: {} on page: {} attempt: {}", region, pageNo, attempts);
                    if (attempts >= maxAttempts) {
                        log.error("Failed to fetch data for region: {} after {} attempts", region, maxAttempts);
                        emailService.sendSimpleMessageToDefaultRecipient("Problem with processing region - " + region, "No data found for region - " + region);
                        break;
                    }
                }
            } catch (Exception e) {
                attempts++;
                log.error("Error fetching data for region: {} on attempt: {} due to: {}", region, attempts, e.getMessage());
                if (attempts >= maxAttempts) {
                    emailService.sendSimpleMessageToDefaultRecipient("Problem with processing region - " + region, "Error: " + e.getMessage());
                    break;
                }
            }
        }
    }


    private HouseResponse getHouseResponse(Region region, int pageNumber) {
        return houseRegistryClient.getHouses(region, pageNumber);
    }

    //w porownaniu ze zwyklym takim pojedynczym update po kolei - srednio ubylo 13 sekund per wszystkie regiony
    private void insertHousesToDB(List<House> houses, Region region) {
        String sql = "INSERT INTO HOUSE(id, area, price, rooms, description, region, type, creation_date) VALUES(?,?,?,?,?,?,?,?)";
        List<Object[]> batchArgs = new ArrayList<>();
        for (House house : houses) {
            house.setRegion(region);
            house.setCreationDate(LocalDate.from(dateProvider.now()));
            Object[] houseData = {
                    house.getId(),
                    house.getArea(),
                    house.getPrice(),
                    house.getRooms(),
                    house.getDescription(),
                    house.getRegion().name(),
                    house.getType(),
                    house.getCreationDate()
            };
            batchArgs.add(houseData);
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }


    @Transactional(readOnly = true)
    public AverageHouseValueResponse getAveragePrice(String regionID, String size, Integer rooms, List<String> types, LocalDate dateSince, LocalDate dateUntil) {
        if (Arrays.stream(Region.values()).noneMatch(r -> r.name().equals(regionID))) {
            throw new RegionNotExistsException(regionID);
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<House> house = query.from(House.class);

        List<Predicate> predicates = createPredicates(regionID, size, rooms, types, dateSince, dateUntil, cb, house);

        query.select(cb.toBigDecimal(cb.avg(house.get("price")))).where(cb.and(predicates.toArray(new Predicate[0])));

        BigDecimal result = entityManager.createQuery(query).getSingleResult();
        return new AverageHouseValueResponse(result);
    }

    private List<Predicate> createPredicates(String regionID, String size, Integer rooms, List<String> types, LocalDate dateSince, LocalDate dateUntil, CriteriaBuilder cb, Root<House> house) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(house.get("region"), regionID));

        if (size != null) {
            switch (size) {
                case "S":
                    predicates.add(cb.between(house.get("area"), 18, 45));
                    break;
                case "M":
                    predicates.add(cb.between(house.get("area"), 46, 80));
                    break;
                case "L":
                    predicates.add(cb.between(house.get("area"), 81, 400));
                    break;
                default:
                    throw new SizeNotExistsException(size);
            }
        }
        if (rooms != null) {
            predicates.add(cb.equal(house.get("rooms"), rooms));
        }
        if (types != null && !types.isEmpty()) {
            predicates.add(house.get("type").in(types));
        }
        if (dateSince != null) {
            predicates.add(cb.greaterThanOrEqualTo(house.get("creationDate"), dateSince));
        }
        if (dateUntil != null) {
            predicates.add(cb.lessThanOrEqualTo(house.get("creationDate"), dateUntil));
        }

        return predicates;
    }
}
