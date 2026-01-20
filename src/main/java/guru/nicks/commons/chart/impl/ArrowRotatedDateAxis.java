package guru.nicks.commons.chart.impl;

import lombok.Getter;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Custom {@link RotatedDateAxis} with arrow at the end of the axis line. The rotation logic is handled by
 * {@link RotatedDateAxis}, while this class adds arrow drawing functionality.
 */
public class ArrowRotatedDateAxis extends RotatedDateAxis {

    private static final double ARROW_SIZE = 10.0;

    /**
     * Whether to position the axis label to the right of the chart (below the arrow).
     */
    @Getter
    private final boolean labelOnRight;

    /**
     * The direction in which the arrow should point.
     */
    @Getter
    private final ArrowDirection arrowDirection;

    public ArrowRotatedDateAxis(double rotationAngle) {
        super(rotationAngle);
        labelOnRight = false;
        arrowDirection = ArrowDirection.AUTO;
    }

    public ArrowRotatedDateAxis(String label, double rotationAngle) {
        super(label, rotationAngle);
        labelOnRight = false;
        arrowDirection = ArrowDirection.AUTO;
    }

    public ArrowRotatedDateAxis(String label, double rotationAngle, TimeZone timeZone) {
        super(label, rotationAngle, timeZone);
        labelOnRight = false;
        arrowDirection = ArrowDirection.AUTO;
    }

    public ArrowRotatedDateAxis(String label, double rotationAngle, TimeZone timeZone, Locale locale) {
        super(label, rotationAngle, timeZone, locale);
        labelOnRight = false;
        arrowDirection = ArrowDirection.AUTO;
    }

    /**
     * Constructor with label positioning option.
     *
     * @param label         the axis label
     * @param rotationAngle the angle for tick labels rotation
     * @param timeZone      the time zone
     * @param locale        the locale
     * @param labelOnRight  if true, position the label to the right of the chart (below the arrow)
     */
    public ArrowRotatedDateAxis(String label, double rotationAngle, TimeZone timeZone, Locale locale,
            boolean labelOnRight) {
        super(label, rotationAngle, timeZone, locale);
        this.labelOnRight = labelOnRight;
        this.arrowDirection = ArrowDirection.AUTO;
    }

    /**
     * Constructor with arrow direction option.
     *
     * @param label          the axis label
     * @param rotationAngle  the angle for tick labels rotation
     * @param timeZone       the time zone
     * @param locale         the locale
     * @param arrowDirection the direction in which the arrow should point
     */
    public ArrowRotatedDateAxis(String label, double rotationAngle, TimeZone timeZone, Locale locale,
            ArrowDirection arrowDirection) {
        super(label, rotationAngle, timeZone, locale);
        this.labelOnRight = false;
        this.arrowDirection = arrowDirection;
    }

    /**
     * Constructor with label positioning and arrow direction options.
     *
     * @param label          the axis label
     * @param rotationAngle  the angle for tick labels rotation
     * @param timeZone       the time zone
     * @param locale         the locale
     * @param labelOnRight   if true, position the label to the right of the chart (below the arrow)
     * @param arrowDirection the direction in which the arrow should point
     */
    public ArrowRotatedDateAxis(String label, double rotationAngle, TimeZone timeZone, Locale locale,
            boolean labelOnRight, ArrowDirection arrowDirection) {
        super(label, rotationAngle, timeZone, locale);
        this.labelOnRight = labelOnRight;
        this.arrowDirection = arrowDirection;
    }

    /**
     * Augments the drawing of the axis by adding an arrow at the end of the axis line.
     */
    @Override
    public AxisState draw(java.awt.Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea,
            RectangleEdge edge, PlotRenderingInfo info) {
        String originalLabel = null;

        // If label should be on right, temporarily hide the default label
        if (labelOnRight && (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM)) {
            originalLabel = getLabel();
            if (originalLabel != null) {
                setLabel(null);
            }
        }

        AxisState state = super.draw(g2, cursor, plotArea, dataArea, edge, info);

        // Restore the label and draw it on the right if needed
        if (labelOnRight && (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) && originalLabel != null) {
            setLabel(originalLabel);
            drawLabelOnRight(g2, plotArea, dataArea, edge, state.getCursor());
        }

        drawArrow(g2, cursor, dataArea, edge);
        return state;
    }

    /**
     * Draws the axis label to the right of the chart, below the arrow.
     */
    private void drawLabelOnRight(Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge,
            double cursor) {
        String label = getLabel();

        if ((label == null) || label.isBlank()) {
            return;
        }

        AffineTransform savedTransform = g2.getTransform();
        Font savedFont = g2.getFont();
        Paint savedPaint = g2.getPaint();
        Font labelFont = getLabelFont();
        Paint labelPaint = getLabelPaint();

        try {
            g2.setFont(labelFont);
            g2.setPaint(labelPaint);

            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D labelBounds = fm.getStringBounds(label, g2);

            // Position label to the right of the chart, inside the data area.
            // Use plotArea to ensure we stay within visible chart boundaries
            double x = Math.min(dataArea.getMaxX(), plotArea.getMaxX()) - labelBounds.getWidth();
            double y = cursor;

            // For bottom edge, position below the cursor.
            // For top edge, position above the cursor.
            if (edge == RectangleEdge.BOTTOM) {
                y += labelBounds.getHeight() + 5;
            } else {
                y -= 5;
            }

            g2.drawString(label, (float) x, (float) y);
        } finally {
            g2.setFont(savedFont);
            g2.setPaint(savedPaint);
            g2.setTransform(savedTransform);
        }
    }

    private void drawArrow(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge) {
        Paint originalPaint = g2.getPaint();
        Stroke originalStroke = g2.getStroke();
        g2.setPaint(getAxisLinePaint());
        g2.setStroke(getAxisLineStroke());

        double x1;
        double y1;
        double x2;
        double y2;
        boolean horizontal;

        // Determine arrow direction based on setting or edge position
        horizontal = switch (arrowDirection) {
            case HORIZONTAL -> true;
            case VERTICAL -> false;
            case AUTO -> (edge == RectangleEdge.TOP) || (edge == RectangleEdge.BOTTOM);
        };

        if (horizontal) {
            // Horizontal arrow at right end, using cursor for Y position
            x1 = dataArea.getMaxX();
            y1 = cursor;
            x2 = dataArea.getMaxX() + ARROW_SIZE;
            y2 = cursor;
        } else {
            // Vertical arrow at top end, using cursor for X position
            x1 = cursor;
            y1 = dataArea.getMinY();
            x2 = cursor;
            y2 = dataArea.getMinY() - ARROW_SIZE;
        }

        drawArrowHead(g2, x1, y1, x2, y2, horizontal);

        g2.setPaint(originalPaint);
        g2.setStroke(originalStroke);
    }

    private void drawArrowHead(Graphics2D g2, double x1, double y1, double x2, double y2, boolean horizontal) {
        // arrow pointing right
        if (horizontal) {
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
            // arrow head
            g2.draw(new Line2D.Double(x2, y2, x2 - 5, y2 - 3));
            g2.draw(new Line2D.Double(x2, y2, x2 - 5, y2 + 3));
            return;
        }

        // arrow pointing up
        g2.draw(new Line2D.Double(x1, y1, x2, y2));
        // arrow head
        g2.draw(new Line2D.Double(x2, y2, x2 - 3, y2 + 5));
        g2.draw(new Line2D.Double(x2, y2, x2 + 3, y2 + 5));
    }

    /**
     * Arrow direction options.
     */
    public enum ArrowDirection {
        /**
         * Arrow points vertically (up or down depending on position)
         */
        VERTICAL,
        /**
         * Arrow points horizontally (left or right depending on position)
         */
        HORIZONTAL,
        /**
         * Arrow direction is determined automatically based on axis edge position
         */
        AUTO
    }

}
