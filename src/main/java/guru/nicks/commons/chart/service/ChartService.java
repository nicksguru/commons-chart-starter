package guru.nicks.commons.chart.service;

import guru.nicks.commons.chart.domain.CountByDateChartRequest;

import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.RectangleInsets;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Service for generating visualizations.
 */
public interface ChartService {

    /**
     * Width or height.
     */
    int MIN_IMAGE_DIMENSION = 30;

    /**
     * Width or height.
     */
    int MAX_IMAGE_DIMENSION = 2000;

    /**
     * Maximum number of data points.
     */
    int MAX_DATA_POINTS = 1000;

    /**
     * Default font family name used throughout chart elements.
     *
     * @see #getFontName()
     */
    String DEFAULT_FONT_NAME = "Segoe UI";

    /**
     * Default font size for chart title text.
     *
     * @see #getTitleFontSize()
     */
    int DEFAULT_TITLE_FONT_SIZE = 18;

    /**
     * Default font size for legend items.
     */
    int DEFAULT_LEGEND_ITEM_FONT_SIZE = 12;

    /**
     * Default font size for data item labels.
     *
     * @see #getItemLabelFontSize()
     */
    int DEFAULT_ITEM_LABEL_FONT_SIZE = 10;

    /**
     * Default font size for axis tick labels.
     */
    int DEFAULT_TICK_LABEL_FONT_SIZE = 10;

    /**
     * Default font size for axis label text.
     *
     * @see #getAxisLabelFontSize()
     */
    int DEFAULT_AXIS_LABEL_FONT_SIZE = 13;

    /**
     * Default color for gradient bar charts (indigo).
     *
     * @see #getBarColorPrimary()
     */
    Color DEFAULT_BAR_COLOR_PRIMARY = new Color(99, 102, 241);

    /**
     * Default color for gradient bar charts (violet).
     *
     * @see #getBarColorSecondary()
     */
    Color DEFAULT_BAR_COLOR_SECONDARY = new Color(139, 92, 246);

    /**
     * Default color for trend lines (rose-red).
     */
    Color DEFAULT_LINE_COLOR = new Color(244, 63, 94);

    /**
     * Default background color for the entire chart area (light grayish-white).
     *
     * @see #getBackgroundColor()
     */
    Color DEFAULT_BACKGROUND_COLOR = new Color(250, 250, 252);

    /**
     * Default background color for the plot area (pure white).
     */
    Color DEFAULT_PLOT_BACKGROUND_COLOR = new Color(255, 255, 255);

    /**
     * Default color for grid lines (light gray).
     *
     * @see #getGridLineColor()
     */
    Color DEFAULT_GRID_LINE_COLOR = new Color(229, 231, 235);

    /**
     * Default color for chart title text (dark gray).
     */
    Color DEFAULT_TITLE_COLOR = new Color(31, 41, 55);

    /**
     * Default gray color for axis elements.
     *
     * @see #getAxisGrayColor()
     */
    Color DEFAULT_AXIS_GRAY_COLOR = Color.GRAY;

    /**
     * Default margin factor for bar chart elements.
     *
     * @see #getBarMargin()
     */
    double DEFAULT_BAR_MARGIN = 0.7;

    /**
     * Default alignment factor for bar positioning (0.5 = centered).
     */
    double DEFAULT_BAR_ALIGNMENT_FACTOR = 0.5;

    /**
     * Padding insets for the legend box.
     *
     * @see #getLegendPadding()
     */
    RectangleInsets DEFAULT_LEGEND_PADDING = new RectangleInsets(2.0, 5.0, 2.0, 5.0);

    /**
     * Padding insets for the overall chart area.
     *
     * @see #getChartPadding()
     */
    RectangleInsets DEFAULT_CHART_PADDING = new RectangleInsets(7.0, 5.0, 27.0, 5.0);

    /**
     * Diamond-shaped point marker for line dataset data points.
     * <p>
     * WARNING: this object is a singleton and therefore must not be modified!
     *
     * @see #getPointShape()
     */
    Polygon DEFAULT_POINT_SHAPE = new Polygon(new int[]{5, 0, -5, 0}, new int[]{0, 5, 0, -5}, 4);

    /**
     * Generates a PNG chart of counts over time. The dataset is displayed both as bars and as a line connecting the
     * actual values. The X axis (date labels) is rotated for better readability, and only has ticks for the dates
     * present in the dataset.
     *
     * @param request      chart generation parameters
     * @param outputStream output stream for PNG
     * @throws IOException error rendering chart
     */
    void generateCountByDatePngChart(CountByDateChartRequest request, OutputStream outputStream) throws IOException;

    /**
     * Creates (but does not configure) a bar dataset renderer instance.
     *
     * @return bar dataset renderer
     */
    default XYBarRenderer createBarDatasetRenderer() {
        return new XYBarRenderer(getBarMargin());
    }

    /**
     * Default implementation returns {@link #DEFAULT_BAR_COLOR_PRIMARY}.
     */
    default Color getBarColorPrimary() {
        return DEFAULT_BAR_COLOR_PRIMARY;
    }

    /**
     * Default implementation returns {@link #DEFAULT_BAR_COLOR_SECONDARY}.
     */
    default Color getBarColorSecondary() {
        return DEFAULT_BAR_COLOR_SECONDARY;
    }

    /**
     * Creates (but does not configure) a line dataset renderer instance.
     * <p>
     * NOTE: {@link XYSplineRenderer} creates waves whose peaks go beyond the chart boundary. Also, such peaks are
     * misleading - they don't represent actual data points and hint at a trend that doesn't exist (lower and higher
     * values).
     *
     * @return line dataset renderer
     */
    default XYItemRenderer createLineDatasetRenderer() {
        return new XYLineAndShapeRenderer();
    }

    /**
     * Default implementation returns {@link #DEFAULT_LINE_COLOR}.
     */
    default Color getLineColor() {
        return DEFAULT_LINE_COLOR;
    }

    /**
     * Default implementation returns {@link #DEFAULT_BACKGROUND_COLOR}.
     */
    default Color getBackgroundColor() {
        return DEFAULT_BACKGROUND_COLOR;
    }

    /**
     * Default implementation returns {@link #DEFAULT_PLOT_BACKGROUND_COLOR}.
     */
    default Color getPlotBackgroundColor() {
        return DEFAULT_PLOT_BACKGROUND_COLOR;
    }

    /**
     * Default implementation returns {@link #DEFAULT_GRID_LINE_COLOR}.
     */
    default Color getGridLineColor() {
        return DEFAULT_GRID_LINE_COLOR;
    }

    /**
     * Default implementation returns {@link #DEFAULT_TITLE_COLOR}.
     */
    default Color getTitleColor() {
        return DEFAULT_TITLE_COLOR;
    }

    /**
     * Default implementation returns {@link #DEFAULT_AXIS_GRAY_COLOR}.
     */
    default Color getAxisGrayColor() {
        return DEFAULT_AXIS_GRAY_COLOR;
    }

    /**
     * Default implementation returns {@link #DEFAULT_BAR_MARGIN}.
     */
    default double getBarMargin() {
        return DEFAULT_BAR_MARGIN;
    }

    /**
     * Default implementation returns {@link #DEFAULT_BAR_ALIGNMENT_FACTOR}.
     */
    default double getBarAlignmentFactor() {
        return DEFAULT_BAR_ALIGNMENT_FACTOR;
    }

    /**
     * Default implementation returns {@link #DEFAULT_TITLE_FONT_SIZE}.
     */
    default int getTitleFontSize() {
        return DEFAULT_TITLE_FONT_SIZE;
    }

    /**
     * Default implementation returns {@link #DEFAULT_LEGEND_ITEM_FONT_SIZE}.
     */
    default int getLegendItemFontSize() {
        return DEFAULT_LEGEND_ITEM_FONT_SIZE;
    }

    /**
     * Default implementation returns {@link #DEFAULT_ITEM_LABEL_FONT_SIZE}.
     */
    default int getItemLabelFontSize() {
        return DEFAULT_ITEM_LABEL_FONT_SIZE;
    }

    /**
     * Default implementation returns {@link #DEFAULT_TICK_LABEL_FONT_SIZE}.
     */
    default int getTickLabelFontSize() {
        return DEFAULT_TICK_LABEL_FONT_SIZE;
    }

    /**
     * Default implementation returns {@link #DEFAULT_AXIS_LABEL_FONT_SIZE}.
     */
    default int getAxisLabelFontSize() {
        return DEFAULT_AXIS_LABEL_FONT_SIZE;
    }

    /**
     * Default implementation returns {@link #DEFAULT_LEGEND_PADDING}.
     */
    default RectangleInsets getLegendPadding() {
        return DEFAULT_LEGEND_PADDING;
    }

    /**
     * Default implementation returns {@link #DEFAULT_CHART_PADDING}.
     */
    default RectangleInsets getChartPadding() {
        return DEFAULT_CHART_PADDING;
    }

    /**
     * Default implementation returns {@link #DEFAULT_POINT_SHAPE}.
     */
    default Polygon getPointShape() {
        return DEFAULT_POINT_SHAPE;
    }

    /**
     * Default implementation returns {@link #DEFAULT_FONT_NAME}.
     */
    default String getFontName() {
        return DEFAULT_FONT_NAME;
    }

}
