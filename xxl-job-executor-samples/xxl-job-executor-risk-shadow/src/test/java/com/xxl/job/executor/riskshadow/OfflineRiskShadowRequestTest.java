package com.xxl.job.executor.riskshadow;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfflineRiskShadowRequestTest {
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-13T11:05:47Z"), ZoneOffset.UTC);

    @Test
    void defaultsScheduledRunsToTheCurrentUtcHour() {
        OfflineRiskShadowRequest request = OfflineRiskShadowRequest.parse("{}", CLOCK);
        assertThat(request.asOf()).isEqualTo(Instant.parse("2026-11-13T11:00:00Z"));
    }

    @Test
    void acceptsOnlyAnExplicitRfc3339AsOfForHistoricalComparison() {
        OfflineRiskShadowRequest request = OfflineRiskShadowRequest.parse(
                "{\"as_of\":\"2026-11-12T08:00:00Z\"}", CLOCK);
        assertThat(request.asOf()).isEqualTo(Instant.parse("2026-11-12T08:00:00Z"));
    }

    @Test
    void rejectsArbitraryExecutionParameters() {
        assertThatThrownBy(() -> OfflineRiskShadowRequest.parse(
                "{\"as_of\":\"2026-11-12T08:00:00Z\",\"command\":\"curl example.test\"}", CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsupported field");
    }
}
