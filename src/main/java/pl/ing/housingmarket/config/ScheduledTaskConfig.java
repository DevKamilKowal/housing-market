package pl.ing.housingmarket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import pl.ing.housingmarket.service.HouseService;
import pl.ing.housingmarket.service.provider.DateProvider;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class ScheduledTaskConfig {
    private final DateProvider dateProvider;

    @Value("${house.fetching.time}")
    private String taskTime;

    @Bean
    public TaskScheduler taskScheduler(HouseService houseService) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();

        String cronExpression = convertTimeToCronExpression(taskTime);

        scheduler.schedule(houseService::fetchAllHouses,
                triggerContext -> {
                    CronExpression expression = CronExpression.parse(cronExpression);
                    ZonedDateTime nextExecutionTime = Optional.ofNullable(dateProvider.now())
                            .map(dateTime -> dateTime.atZone(ZoneId.systemDefault()))
                            .orElse(ZonedDateTime.now(ZoneId.systemDefault()));
                    return expression.next(nextExecutionTime).toInstant();
                }
        );

        return scheduler;
    }

    private String convertTimeToCronExpression(String time) {
        LocalTime parsedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        return String.format("0 %d %d * * ?", parsedTime.getMinute(), parsedTime.getHour());
    }
}


