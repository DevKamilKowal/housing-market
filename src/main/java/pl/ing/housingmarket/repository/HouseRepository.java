package pl.ing.housingmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ing.housingmarket.model.House;

import java.util.UUID;

public interface HouseRepository extends JpaRepository<House, UUID> {


}
