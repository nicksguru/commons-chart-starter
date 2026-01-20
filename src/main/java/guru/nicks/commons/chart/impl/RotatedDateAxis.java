package guru.nicks.commons.chart.impl;

import lombok.EqualsAndHashCode;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTick;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Custom {@link DateAxis} that rotates tick labels. This improves readability of long/dense date labels on the X-axis
 * by angling them.
 */
@EqualsAndHashCode(callSuper = false)
public class RotatedDateAxis extends DateAxis {

    /**
     * Rotation angle in radians for tick labels (-45 degrees).
     */
    private final double rotationAngle;

    /**
     * Creates a new RotatedDateAxis with default settings.
     */
    public RotatedDateAxis(double rotationAngle) {
        super();
        this.rotationAngle = rotationAngle;
    }

    /**
     * Creates a new RotatedDateAxis with the specified label.
     *
     * @param label the axis label
     */
    public RotatedDateAxis(String label, double rotationAngle) {
        super(label);
        this.rotationAngle = rotationAngle;
    }

    /**
     * Creates a new RotatedDateAxis with the specified label and time zone.
     *
     * @param label    the axis label
     * @param timeZone the time zone
     */
    public RotatedDateAxis(String label, double rotationAngle, TimeZone timeZone) {
        super(label, timeZone, Locale.getDefault());
        this.rotationAngle = rotationAngle;
    }

    /**
     * Creates a new RotatedDateAxis with the specified label, locale, and time zone.
     *
     * @param label    the axis label
     * @param locale   the locale
     * @param timeZone the time zone
     */
    public RotatedDateAxis(String label, double rotationAngle, Locale locale, TimeZone timeZone) {
        super(label, timeZone, locale);
        this.rotationAngle = rotationAngle;
    }

    /**
     * Override to apply rotation to horizontal tick labels.
     *
     * @param g2       the graphics device
     * @param dataArea the data area for the plot
     * @param edge     the edge that the axis is aligned to
     * @return list of rotated ticks
     */
    @Override
    protected List<DateTick> refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        // get ticks from parent class
        List<DateTick> parentTicks = super.refreshTicksHorizontal(g2, dataArea, edge);

        return parentTicks.stream()
                .map(tick -> new DateTick(tick.getDate(), tick.getText(),
                        TextAnchor.TOP_RIGHT, TextAnchor.CENTER_RIGHT,
                        rotationAngle))
                .toList();
    }

}
