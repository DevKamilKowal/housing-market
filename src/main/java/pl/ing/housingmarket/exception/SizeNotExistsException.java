package pl.ing.housingmarket.exception;

import lombok.Value;

@Value
public class SizeNotExistsException extends RuntimeException {
    String size;
}
