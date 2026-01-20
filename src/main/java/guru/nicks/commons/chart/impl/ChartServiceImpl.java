package guru.nicks.commons.chart.impl;

import guru.nicks.commons.chart.domain.CountByDate;
import guru.nicks.commons.chart.domain.CountByDateChartRequest;
import guru.nicks.commons.chart.domain.DateScale;
import guru.nicks.commons.chart.service.ChartService;
import guru.nicks.commons.validation.AnnotationValidator;

import am.ik.yavi.meta.ConstraintArguments;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TimeZone;

@RequiredArgsConstructor
@Slf4j
public class ChartServiceImpl implements ChartService {

    private static final int BAR_DATASET_INDEX = 0;
    private static final int LINE_DATASET_INDEX = 1;

    @NonNull // Lombok creates runtime nullness check for this own annotation only
    private final AnnotationValidator annotationValidator;

    @ConstraintArguments
    @Override
    public void generateCountByDatePngChart(CountByDateChartRequest request, OutputStream outputStream)
            throws IOException {
        // instead of @Valid which may not be enabled by default
        annotationValidator.validate(request);
        log.debug("Generating PNG chart for {} data points: {}", request.data().size(), request);

        // create separate datasets for each series - to enable different renderers
        var barSeries = new TimeSeries(request.yAxisLabel());
        var barDataset = new TimeSeriesCollection(barSeries, request.timeZone());

        var lineSeries = new TimeSeries(request.trendTitle());
        var lineDataset = new TimeSeriesCollection(lineSeries, request.timeZone());

        // populate time series, avoid calling these getters for all (numerous) data points
        DateScale dateScale = request.dateScale();
        TimeZone timeZone = request.timeZone();
        Locale dateLocale = request.dateLocale();

        for (CountByDate dataItem : request.data()) {
            dateScale.addToTimeSeries(barSeries, dataItem, timeZone, dateLocale);
            dateScale.addToTimeSeries(lineSeries, dataItem, timeZone, dateLocale);
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(request.chartTitle(),
                request.xAxisLabel(), request.yAxisLabel(),
                // dataset with index 0
                barDataset,
                // legend
                true,
                // tooltips
                false,
                // URLs
                false);
        XYPlot plot = (XYPlot) chart.getPlot();
        // add the second dataset (with index 1)
        plot.setDataset(LINE_DATASET_INDEX, lineDataset);

        // show int numbers (such as 1) instead of scientific notation (such as 1E0).  DecimalFormat is not thread-safe.
        var countFormatter = NumberFormat.getIntegerInstance(request.dateLocale());
        configureChartBackground(chart);

        configurePlot(plot);
        configureXaxis(plot, request.dateLocale(), request.timeZone());
        configureYaxis(plot, countFormatter);

        plot.setRenderer(BAR_DATASET_INDEX, configureBarDatasetRenderer(countFormatter));
        plot.setRenderer(LINE_DATASET_INDEX, configureLineDatasetRenderer());
        renderChartAsPng(chart, request.width(), request.height(), outputStream);
    }

    /**
     * Configures the chart background, title, legend, and padding settings.
     *
     * @param chart chart to configure
     */
    protected void configureChartBackground(JFreeChart chart) {
        chart.setBackgroundPaint(getBackgroundColor());

        // style the title
        var title = chart.getTitle();
        title.setPaint(getTitleColor());
        title.setFont(new Font(getFontName(), Font.BOLD, getTitleFontSize()));

        // style chart legend
        var legend = chart.getLegend();
        legend.setItemFont(new Font(getFontName(), Font.PLAIN, getLegendItemFontSize()));
        legend.setBackgroundPaint(Color.WHITE);
        legend.setFrame(new BlockBorder(Color.LIGHT_GRAY));

        // move legend to top-right corner to avoid overlapping rotated X-axis labels (dates, they're quite long)
        legend.setPosition(RectangleEdge.TOP);
        legend.setHorizontalAlignment(HorizontalAlignment.CENTER);
        legend.setVerticalAlignment(VerticalAlignment.TOP);
        legend.setPadding(getLegendPadding());

        // add bottom padding to give rotated X-axis ticks enough room (the legend isn't there anymore, so without
        // this, the chart height is too small)
        chart.setPadding(getChartPadding());
    }

    /**
     * Creates and configures a bar dataset renderer with gradient paint, item labels, and styling.
     *
     * @param countFormatter number formatter for displaying bar values
     * @return configured renderer
     */
    protected XYItemRenderer configureBarDatasetRenderer(NumberFormat countFormatter) {
        var renderer = createBarDatasetRenderer();
        renderer.setBarAlignmentFactor(getBarAlignmentFactor());

        // add gradient paint to bars
        var gradientPaint = new GradientPaint(0, 0, getBarColorPrimary(), 0, 100, getBarColorSecondary());
        renderer.setSeriesPaint(0, gradientPaint);

        // make bars look more stylish with standard bar painter and no shadow
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardXYBarPainter());

        // show Y value on top of each bar with styling
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelPaint(Color.DARK_GRAY);
        renderer.setDefaultItemLabelFont(new Font(getFontName(), Font.PLAIN, getItemLabelFontSize()));

        renderer.setDefaultItemLabelGenerator((theDataset, seriesIndex, itemIndex) -> {
            double value = theDataset.getYValue(seriesIndex, itemIndex);
            return countFormatter.format(value);
        });

        return renderer;
    }

    /**
     * Creates and configures a line dataset renderer with diamond markers.
     *
     * @return configured renderer
     */
    protected XYItemRenderer configureLineDatasetRenderer() {
        var renderer = createLineDatasetRenderer();
        renderer.setSeriesPaint(0, getLineColor());
        renderer.setSeriesStroke(0, new BasicStroke(2.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // create diamond shape markers with fill
        renderer.setSeriesShape(0, getPointShape());

        if (renderer instanceof XYLineAndShapeRenderer s) {
            s.setSeriesShapesFilled(0, true);
            s.setSeriesShapesVisible(0, true);
        } else {
            log.warn("Line renderer is not {}, shape configuration skipped", XYLineAndShapeRenderer.class.getName());
        }

        renderer.setSeriesOutlinePaint(0, getLineColor());
        renderer.setSeriesOutlineStroke(0, new BasicStroke(2));
        renderer.setSeriesFillPaint(0, Color.WHITE);

        return renderer;
    }

    /**
     * Configures the plot background, grid lines, and border settings.
     *
     * @param plot plot to configure
     */
    protected void configurePlot(XYPlot plot) {
        plot.setBackgroundPaint(getPlotBackgroundColor());
        plot.setDomainGridlinePaint(getGridLineColor());
        plot.setRangeGridlinePaint(getGridLineColor());

        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        plot.setDomainGridlineStroke(new BasicStroke(0.5F));
        plot.setRangeGridlineStroke(new BasicStroke(0.5F));

        // remove outer border for cleaner look
        plot.setOutlineVisible(false);
    }

    /**
     * Configures the X-axis with rotated tick labels and date formatting.
     *
     * @param plot       plot containing the X-axis
     * @param dateLocale locale for date formatting
     * @param timeZone   timezone for date formatting
     */
    protected void configureXaxis(XYPlot plot, Locale dateLocale, TimeZone timeZone) {
        // create custom with rotated tick labels (-45 degrees, for long and dense dates) and arrow
        // position the X axis label to the right of the chart (below the arrow)
        var xAxis = new ArrowRotatedDateAxis(plot.getDomainAxis().getLabel(), -Math.PI / 4,
                timeZone, dateLocale, true);
        // set the dataset so the axis can generate ticks only for dates present in the data
        if (plot.getDataset(BAR_DATASET_INDEX) instanceof TimeSeriesCollection collection) {
            xAxis.setDataset(collection);
        }

        plot.setDomainAxis(xAxis);

        // e.g. 'Jan 1, 2026' (the month name is abbreviated)
        var dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, dateLocale);
        // WARNING: without this, formatting Date objects (this is what JFreeChart uses for date axes) will default to
        // UTC - because a Date is always in UTC
        dateFormat.setTimeZone(timeZone);
        xAxis.setDateFormatOverride(dateFormat);

        xAxis.setTickLabelFont(new Font(getFontName(), Font.PLAIN, getTickLabelFontSize()));
        xAxis.setTickMarkPaint(getAxisGrayColor());
        xAxis.setAxisLinePaint(getAxisGrayColor());
        xAxis.setLabelFont(new Font(getFontName(), Font.BOLD, getAxisLabelFontSize()));
    }

    /**
     * Configures the Y-axis with integer formatting and styling.
     *
     * @param plot           plot containing the Y-axis
     * @param countFormatter number formatter for displaying tick values
     */
    protected void configureYaxis(XYPlot plot, NumberFormat countFormatter) {
        var yAxis = new ArrowNumberAxis(plot.getRangeAxis().getLabel());
        plot.setRangeAxis(yAxis);

        yAxis.setNumberFormatOverride(countFormatter);
        yAxis.setAutoRangeIncludesZero(true);

        // configure tick units to show only integer values (0, 1, 2, 3, ...) without duplicates
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setTickUnit(new NumberTickUnit(1.0, countFormatter));

        yAxis.setTickLabelFont(new Font(getFontName(), Font.PLAIN, getTickLabelFontSize()));
        yAxis.setTickMarkPaint(getAxisGrayColor());
        yAxis.setAxisLinePaint(getAxisGrayColor());
        yAxis.setLabelFont(new Font(getFontName(), Font.BOLD, getAxisLabelFontSize()));
    }

    /**
     * Renders the chart as a PNG image with antialiasing enabled.
     *
     * @param chart        chart to render
     * @param width        image width in pixels
     * @param height       image height in pixels
     * @param outputStream output stream to write the PNG image to
     * @throws IOException if an error occurs while writing the image
     */
    protected void renderChartAsPng(JFreeChart chart, int width, int height, OutputStream outputStream)
            throws IOException {
        // enable antialiasing for high-quality rendering
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        BufferedImage image = null;
        try {
            image = chart.createBufferedImage(width, height);
            ChartUtils.writeBufferedImageAsPNG(outputStream, image);
        } finally {
            // release native resources
            if (image != null) {
                image.flush();
            }
        }
    }

}
