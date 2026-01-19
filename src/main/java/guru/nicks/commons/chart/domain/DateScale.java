package guru.nicks.commons.chart.domain;

import am.ik.yavi.meta.ConstraintArguments;
import jakarta.annotation.Nonnull;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static guru.nicks.commons.validation.dsl.ValiDsl.checkNotNull;

/**
 * Date scale for grouping orders in statistics. The dates are truncated, accordingly, to the beginning of the
 * day/week/month/year.
 */
public enum DateScale {

    /**
     * Group by day.
     */
    DAY,

    /**
     * Group by week.
     */
    WEEK,

    /**
     * Group by month.
     */
    MONTH,

    /**
     * Group by quarter.
     */
    QUARTER,

    /**
     * Group by year.
     */
    YEAR;

    /**
     * Adds a count for the given date to the time series. If the period already exists, adds the count to it.
     *
     * @param timeSeries  time series to add to
     * @param countByDate date and count
     * @param zoneId      timezone for date truncation
     */
    @ConstraintArguments
    public void addToTimeSeries(@Nonnull TimeSeries timeSeries, @Nonnull CountByDate countByDate,
            @Nonnull ZoneId zoneId) {
        checkNotNull(timeSeries, _DateScaleAddToTimeSeriesArgumentsMeta.TIMESERIES.name());
        checkNotNull(countByDate, _DateScaleAddToTimeSeriesArgumentsMeta.COUNTBYDATE.name());
        checkNotNull(zoneId, _DateScaleAddToTimeSeriesArgumentsMeta.ZONEID.name());

        if (countByDate.count() < 0) {
            throw new IllegalArgumentException("Count cannot be negative: " + countByDate.count());
        }

        RegularTimePeriod timePeriod = truncateToRegularTimePeriod(countByDate.date(), zoneId);
        TimeSeriesDataItem dataItem = timeSeries.getDataItem(timePeriod);

        double currentCount = (dataItem != null)
                ? dataItem.getValue().doubleValue()
                : 0.0;

        timeSeries.addOrUpdate(timePeriod, currentCount + countByDate.count());
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
    @ConstraintArguments
    public RegularTimePeriod truncateToRegularTimePeriod(@Nonnull LocalDate date, @Nonnull ZoneId zoneId) {
        checkNotNull(date, _DateScaleTruncateToRegularTimePeriodArgumentsMeta.DATE.name());
        checkNotNull(zoneId, _DateScaleTruncateToRegularTimePeriodArgumentsMeta.ZONEID.name());

        // convert to 00:00:00
        Date utilDate = Date.from(
                date.atStartOfDay(zoneId).toInstant());

        return switch (this) {
            case DAY -> new Day(utilDate);
            case WEEK -> new Week(utilDate);
            case MONTH -> new Month(utilDate);
            case QUARTER -> new Quarter(utilDate);
            case YEAR -> new Year(utilDate);
        };

    }

}
