package guru.nicks.commons.chart.domain;

import guru.nicks.commons.chart.service.ChartService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

/**
 * Request parameters for generating a count by date chart PNG.
 */
@Builder(toBuilder = true)
public record CountByDateChartRequest(

        @NotNull
        @Size(max = ChartService.MAX_DATA_POINTS)
        List<CountByDate> data,

        @NotNull
        DateScale dateScale,

        /**
         * Timezone for date truncation.
         */
        @NotNull
        ZoneId zoneId,

        @Min(ChartService.MIN_IMAGE_DIMENSION)
        @Max(ChartService.MAX_IMAGE_DIMENSION)
        int width,

        @Min(ChartService.MIN_IMAGE_DIMENSION)
        @Max(ChartService.MAX_IMAGE_DIMENSION)
        int height,

        /**
         * For date formatting.
         */
        @NotNull
        Locale dateLocale,

        @NotBlank
        String chartTitle,

        @NotBlank
        String trendTitle,

        @NotBlank
        String xAxisLabel,

        @NotBlank
        String yAxisLabel) {
}
