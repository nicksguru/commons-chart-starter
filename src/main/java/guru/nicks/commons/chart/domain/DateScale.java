package guru.nicks.commons.chart.domain;

import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Date scale for grouping orders in statistics. The dates are truncated, accordingly, to the beginning of the
 * day/week/month/year.
 */
public enum DateScale {

    /**
     * Group by day.
     */
    DAILY,

    /**
     * Group by week.
     */
    WEEKLY,

    /**
     * Group by month.
     */
    MONTHLY,

    /**
     * Group by year.
     */
    YEARLY;

    /**
     * Adds a count for the given date to the time series. If the period already exists, adds the count to it.
     *
     * @param timeSeries  time series to add to
     * @param countByDate date and count
     * @param zoneId      timezone for date truncation
     */
    public void addToTimeSeries(TimeSeries timeSeries, CountByDate countByDate, ZoneId zoneId) {
        RegularTimePeriod timePeriod = truncateToRegularTimePeriod(countByDate.date(), zoneId);

        double count = (timeSeries.getDataItem(timePeriod) != null)
                ? timeSeries.getDataItem(timePeriod).getValue().doubleValue()
                : 0.0;

        timeSeries.addOrUpdate(timePeriod, count + countByDate.count());
    }

    /**
     * Converts the date to a {@link RegularTimePeriod} (beginning of day/week/month/year). For putting it in a
     * {@link TimeSeries}, consider calling {@link #addToTimeSeries(TimeSeries, CountByDate, ZoneId)} because it handles
     * date duplicates smartly.
     *
     * @param date   date to convert
     * @param zoneId timezone for date truncation
     * @return beginning of day/week/month/year
     */
    public RegularTimePeriod truncateToRegularTimePeriod(LocalDate date, ZoneId zoneId) {
        // convert to 00:00:00
        Date utilDate = Date.from(
                date.atStartOfDay(zoneId).toInstant());

        return switch (this) {
            case DAILY -> new Day(utilDate);
            case WEEKLY -> new Week(utilDate);
            case MONTHLY -> new Month(utilDate);
            case YEARLY -> new Year(utilDate);
        };

    }

}
