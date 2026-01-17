package guru.nicks.commons.chart.domain;

import lombok.Builder;

import java.time.LocalDate;

/**
 * Represents the count of something (e.g., orders) for a specific (possibly truncated to the beginning of
 * week/month/year) date, depending on the specified {@link DateScale}.
 *
 */
@Builder(toBuilder = true)
public record CountByDate(

        LocalDate date,
        long count) {

}
