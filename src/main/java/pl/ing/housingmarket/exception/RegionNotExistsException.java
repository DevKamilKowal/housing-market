package pl.ing.housingmarket.exception;

import lombok.Value;

@Value
public class RegionNotExistsException extends RuntimeException {
    String regionID;
}
