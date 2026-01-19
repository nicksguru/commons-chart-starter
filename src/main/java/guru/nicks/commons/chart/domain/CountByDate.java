package guru.nicks.commons.chart.domain;

import lombok.Builder;
import org.springframework.cache.annotation.Cacheable;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents the count of something (e.g., shop orders) for a specific (possibly truncated to the beginning of
 * week/month/year) date, depending on the specified {@link DateScale}.
 * <p>
 * Serializable - for {@link Cacheable @Cacheable}.
 *
 */
@Builder(toBuilder = true)
public record CountByDate(

        LocalDate date,
        long count) implements Serializable {

}
