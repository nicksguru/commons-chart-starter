package guru.nicks.commons.chart.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTick;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

/**
 * Custom {@link DateAxis} that rotates tick labels. This improves readability of long/dense date labels on the X-axis
 * by angling them.
 */
@EqualsAndHashCode(callSuper = true)
public class RotatedDateAxis extends DateAxis {

    /**
     * Rotation angle in radians for tick labels, e.g. {@code -Math.PI / 4} for -45 degrees.
     */
    private final double rotationAngle;

    /**
     * When set, only dates present in this dataset will have ticks.
     */
    @Setter
    private TimeSeriesCollection dataset;

    /**
     * Creates a new RotatedDateAxis with default settings.
     *
     * @param rotationAngle the rotation angle in radians for tick labels
     * @throws IllegalArgumentException if rotationAngle is NaN or infinite
     */
    public RotatedDateAxis(double rotationAngle) {
        super();
        validateRotationAngle(rotationAngle);
        this.rotationAngle = rotationAngle;
    }

    /**
     * Creates a new RotatedDateAxis with the specified label.
     *
     * @param label         the axis label (can be null)
     * @param rotationAngle the rotation angle in radians for tick labels
     * @throws IllegalArgumentException if rotationAngle is NaN or infinite
     */
    public RotatedDateAxis(String label, double rotationAngle) {
        super(label);
        this.rotationAngle = validateRotationAngle(rotationAngle);
    }

    /**
     * Creates a new RotatedDateAxis with the specified label and time zone.
     *
     * @param label         the axis label (can be null)
     * @param rotationAngle the rotation angle in radians for tick labels
     * @param timeZone      the time zone (must not be null)
     * @throws IllegalArgumentException if rotationAngle is NaN or infinite, or if timeZone is null
     */
    public RotatedDateAxis(String label, double rotationAngle, TimeZone timeZone) {
        super(label, timeZone, Locale.getDefault());
        this.rotationAngle = validateRotationAngle(rotationAngle);
    }

    /**
     * Creates a new RotatedDateAxis with the specified label, locale, and time zone.
     *
     * @param label         the axis label (can be null)
     * @param rotationAngle the rotation angle in radians for tick labels
     * @param locale        the locale (must not be null)
     * @param timeZone      the time zone (must not be null)
     * @throws IllegalArgumentException if rotationAngle is NaN or infinite, or if locale or timeZone is null
     */
    public RotatedDateAxis(String label, double rotationAngle, TimeZone timeZone, Locale locale) {
        super(label, timeZone, locale);
        this.rotationAngle = validateRotationAngle(rotationAngle);
    }

    /**
     * Validates the rotation angle.
     *
     * @param angle angle to validate
     * @return validated angle
     * @throws IllegalArgumentException if angle is NaN or infinite
     */
    private static double validateRotationAngle(double angle) {
        if (Double.isNaN(angle) || Double.isInfinite(angle)) {
            throw new IllegalArgumentException("Rotation angle must be a finite number");
        }

        return angle;
    }

    /**
     * Override to apply rotation to horizontal tick labels. If a dataset is set, generates ticks only for dates present
     * in the dataset.
     */
    @Override
    protected List<DateTick> refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        // if dataset is provided, generate ticks only for dataset dates
        List<DateTick> parentTicks = (dataset != null)
                ? generateTicksFromDataset()
                : super.refreshTicksHorizontal(g2, dataArea, edge);

        if (CollectionUtils.isEmpty(parentTicks)) {
            return List.of();
        }

        return parentTicks.stream()
                .filter(Objects::nonNull)
                .map(tick -> new DateTick(tick.getDate(), tick.getText(),
                        TextAnchor.TOP_RIGHT, TextAnchor.CENTER_RIGHT,
                        rotationAngle))
                .toList();
    }

    /**
     * Generates ticks based on the actual dates present in the dataset. This ensures that only dates with data points
     * are shown on the axis.
     *
     * @return list of ticks for dataset dates, sorted by date (never null, but may be empty)
     */
    protected List<DateTick> generateTicksFromDataset() {
        // should not happen if called correctly, but prevents NPE
        if (dataset == null) {
            return List.of();
        }

        DateFormat dateFormat = getDateFormatOverride();
        // use default if not overridden
        if (dateFormat == null) {
            dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, getLocale());
            dateFormat.setTimeZone(getTimeZone());
        }

        // edge case: check for null dates
        Date minDate = getMinimumDate();
        Date maxDate = getMaximumDate();
        if ((minDate == null) || (maxDate == null)) {
            return List.of();
        }

        long minAxisMillis = minDate.getTime();
        long maxAxisMillis = maxDate.getTime();

        Set<Long> seenMillis = new HashSet<>();
        List<DateTick> ticks = new ArrayList<>();

        // collect all unique time periods from all series in the dataset
        for (int seriesIndex = 0, seriesCount = dataset.getSeriesCount(); seriesIndex < seriesCount; seriesIndex++) {
            TimeSeries series = dataset.getSeries(seriesIndex);

            for (int itemIndex = 0, itemCount = series.getItemCount(); itemIndex < itemCount; itemIndex++) {
                var timePeriod = series.getTimePeriod(itemIndex);
                long periodStartMillis = timePeriod.getFirstMillisecond();

                // only add tick if the date is within the axis range and not already seen
                if ((periodStartMillis < minAxisMillis)
                        || (periodStartMillis > maxAxisMillis)
                        || !seenMillis.add(periodStartMillis)) {
                    continue;
                }

                Date date = new Date(periodStartMillis);
                String label = dateFormat.format(date);
                DateTick tick = new DateTick(date, label, TextAnchor.TOP_RIGHT,
                        TextAnchor.CENTER_RIGHT, rotationAngle);
                ticks.add(tick);
            }
        }

        // sort ticks by date
        ticks.sort(Comparator.comparing(DateTick::getDate));
        return ticks;
    }

}
