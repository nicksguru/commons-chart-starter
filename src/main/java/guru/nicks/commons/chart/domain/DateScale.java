package guru.nicks.commons.chart.domain;

import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date scale for grouping orders in statistics. The dates are truncated, accordingly, to the beginning of the
 * day/week/month/year.
 * <p>
 * To avoid discrepancies, please treat local dates as {@link DateScale#TIME_ZONE}.
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

    public static final ZoneId ZONE_ID = ZoneOffset.UTC;
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone(ZONE_ID);

    /**
     * Adds a count for the given date to the time series. If the period already exists, adds the count to it.
     *
     * @param timeSeries  time series to add to
     * @param countByDate date and count
     */
    public void addToTimeSeries(TimeSeries timeSeries, CountByDate countByDate) {
        RegularTimePeriod timePeriod = truncateToRegularTimePeriod(countByDate.date());

        double count = (timeSeries.getDataItem(timePeriod) != null)
                ? timeSeries.getDataItem(timePeriod).getValue().doubleValue()
                : 0.0;

        timeSeries.addOrUpdate(timePeriod, count + countByDate.count());
    }

    /**
     * Converts the date to a {@link RegularTimePeriod} (beginning of day/week/month/year in {@link #ZONE_ID}). For
     * putting it in a {@link TimeSeries}, consider calling {@link #addToTimeSeries(TimeSeries, CountByDate)} because it
     * handles date duplicates smartly.
     *
     * @return beginning of day/week/month/year
     */
    public RegularTimePeriod truncateToRegularTimePeriod(LocalDate date) {
        // convert to 00:00:00
        Date utilDate = Date.from(
                date.atStartOfDay(ZONE_ID).toInstant());

        return switch (this) {
            case DAILY -> new Day(utilDate);
            case WEEKLY -> new Week(utilDate);
            case MONTHLY -> new Month(utilDate);
            case YEARLY -> new Year(utilDate);
        };

    }

}
