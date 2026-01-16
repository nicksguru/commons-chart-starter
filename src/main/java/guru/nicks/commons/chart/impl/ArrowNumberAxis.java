package guru.nicks.commons.chart.impl;

import lombok.Getter;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * Custom {@link NumberAxis} with arrow at the end of the axis line.
 */
public class ArrowNumberAxis extends NumberAxis {

    private static final double ARROW_SIZE = 10.0;

    /**
     * The direction in which the arrow should point.
     */
    @Getter
    private final ArrowDirection arrowDirection;

    public ArrowNumberAxis() {
        super();
        this.arrowDirection = ArrowDirection.VERTICAL;
    }

    public ArrowNumberAxis(String label) {
        super(label);
        this.arrowDirection = ArrowDirection.VERTICAL;
    }

    /**
     * Creates a new axis with the specified arrow direction.
     *
     * @param arrowDirection the direction in which the arrow should point
     */
    public ArrowNumberAxis(ArrowDirection arrowDirection) {
        super();
        this.arrowDirection = arrowDirection;
    }

    /**
     * Creates a new axis with the specified label and arrow direction.
     *
     * @param label          the axis label
     * @param arrowDirection the direction in which the arrow should point
     */
    public ArrowNumberAxis(String label, ArrowDirection arrowDirection) {
        super(label);
        this.arrowDirection = arrowDirection;
    }

    @Override
    public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea,
            RectangleEdge edge, PlotRenderingInfo plotRenderingInfo) {
        AxisState state = super.draw(g2, cursor, plotArea, dataArea, edge, plotRenderingInfo);
        drawArrow(g2, cursor, dataArea);
        return state;
    }

    private void drawArrow(Graphics2D g2, double cursor, Rectangle2D dataArea) {
        Paint savedPaint = g2.getPaint();
        Stroke savedStroke = g2.getStroke();

        g2.setPaint(getAxisLinePaint());
        g2.setStroke(getAxisLineStroke());

        if (arrowDirection == ArrowDirection.HORIZONTAL) {
            // draw horizontal arrow at right end of x-axis, using cursor for Y position
            double x1 = dataArea.getMaxX();
            double y1 = cursor;
            double x2 = dataArea.getMaxX() + ARROW_SIZE;
            double y2 = cursor;
            drawArrowHead(g2, x1, y1, x2, y2, true);
        } else {
            // draw vertical arrow at top of y-axis, using cursor for X position
            double y1 = dataArea.getMinY();
            double y2 = dataArea.getMinY() - ARROW_SIZE;
            drawArrowHead(g2, cursor, y1, cursor, y2, false);
        }

        g2.setPaint(savedPaint);
        g2.setStroke(savedStroke);
    }

    private void drawArrowHead(Graphics2D g2, double x1, double y1, double x2, double y2, boolean horizontal) {
        // draw line for arrow shaft
        g2.draw(new Line2D.Double(x1, y1, x2, y2));

        if (horizontal) {
            // draw arrow head (pointing right)
            g2.draw(new Line2D.Double(x2, y2, x2 - 5, y2 - 3));
            g2.draw(new Line2D.Double(x2, y2, x2 - 5, y2 + 3));
        } else {
            // draw arrow head (pointing up)
            g2.draw(new Line2D.Double(x2, y2, x2 - 3, y2 + 5));
            g2.draw(new Line2D.Double(x2, y2, x2 + 3, y2 + 5));
        }
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
        HORIZONTAL
    }

}
