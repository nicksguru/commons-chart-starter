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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
     * @param timeZone    timezone for {@link CountByDate#date()}
     * @param dateLocale  locale for dates
     */
    @ConstraintArguments
    public void addToTimeSeries(@Nonnull TimeSeries timeSeries, @Nonnull CountByDate countByDate,
            @Nonnull TimeZone timeZone, @Nonnull Locale dateLocale) {
        checkNotNull(timeSeries, _DateScaleAddToTimeSeriesArgumentsMeta.TIMESERIES.name());
        checkNotNull(countByDate, _DateScaleAddToTimeSeriesArgumentsMeta.COUNTBYDATE.name());

        double count = countByDate.count();
        if ((count < 0) || !Double.isFinite(count)) {
            throw new IllegalArgumentException("Count field must be a finite non-negative number");
        }

        RegularTimePeriod timePeriod = truncateToRegularTimePeriod(countByDate.date(), timeZone, dateLocale);
        TimeSeriesDataItem dataItem = timeSeries.getDataItem(timePeriod);

        double currentCount = (dataItem != null) && (dataItem.getValue() != null)
                ? dataItem.getValue().doubleValue()
                : 0.0;

        timeSeries.addOrUpdate(timePeriod, currentCount + countByDate.count());
    }

    /**
     * Converts the date to a {@link RegularTimePeriod} (beginning of day/week/month/year). For putting it in a
     * {@link TimeSeries}, consider calling {@link #addToTimeSeries(TimeSeries, CountByDate, TimeZone, Locale)} because
     * it handles date duplicates smartly.
     *
     * @param date       date to convert
     * @param timeZone   timezone for date truncation
     * @param dateLocale locale for dates
     * @return beginning of day/week/month/year
     */
    @ConstraintArguments
    public RegularTimePeriod truncateToRegularTimePeriod(@Nonnull LocalDate date, @Nonnull TimeZone timeZone,
            @Nonnull Locale dateLocale) {
        checkNotNull(date, _DateScaleTruncateToRegularTimePeriodArgumentsMeta.DATE.name());
        checkNotNull(timeZone, _DateScaleTruncateToRegularTimePeriodArgumentsMeta.TIMEZONE.name());
        checkNotNull(dateLocale, _DateScaleTruncateToRegularTimePeriodArgumentsMeta.DATELOCALE.name());

        // convert to 00:00:00 UTC, but keep the original time zone (see below)
        Date utilDate = Date.from(
                date.atStartOfDay(timeZone.toZoneId()).toInstant());

        return switch (this) {
            case DAY -> new Day(utilDate, timeZone, dateLocale);
            case WEEK -> new Week(utilDate, timeZone, dateLocale);
            case MONTH -> new Month(utilDate, timeZone, dateLocale);
            case QUARTER -> new Quarter(utilDate, timeZone, dateLocale);
            case YEAR -> new Year(utilDate, timeZone, dateLocale);
        };

    }

}
