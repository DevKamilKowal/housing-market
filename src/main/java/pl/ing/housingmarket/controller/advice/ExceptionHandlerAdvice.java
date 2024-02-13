package pl.ing.housingmarket.controller.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.ing.housingmarket.exception.RegionNotExistsException;
import pl.ing.housingmarket.exception.SizeNotExistsException;
import pl.ing.housingmarket.model.response.ErrorResponse;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class ExceptionHandlerAdvice {
    @ExceptionHandler(SizeNotExistsException.class)
    public ResponseEntity<ErrorResponse> handleSizeNotExistsException(SizeNotExistsException exc) {
        return ResponseEntity.status(NOT_FOUND).body(new ErrorResponse("This size isn't supported : " + exc.getSize()));
    }

    @ExceptionHandler(RegionNotExistsException.class)
    public ResponseEntity<ErrorResponse> handleRegionNotExistsException(RegionNotExistsException exc) {
        return ResponseEntity.status(NOT_FOUND).body(new ErrorResponse("This region doesn't exist : " + exc.getRegionID()));
    }
}
