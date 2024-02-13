package pl.ing.housingmarket.model.response;

import pl.ing.housingmarket.model.House;

import java.util.List;

public record HouseResponse(int totalPages, List<House> data) {
}
