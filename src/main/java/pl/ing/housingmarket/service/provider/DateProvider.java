package pl.ing.housingmarket.service.provider;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DateProvider {
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
