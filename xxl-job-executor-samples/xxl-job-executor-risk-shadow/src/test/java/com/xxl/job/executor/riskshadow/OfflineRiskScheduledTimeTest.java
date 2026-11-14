package com.xxl.job.executor.riskshadow;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OfflineRiskScheduledTimeTest {
    private static final Instant SCHEDULED_AT = Instant.parse("2026-11-13T11:47:32Z");

    @Test
    void productionDefaultsToTheScheduledTriggerHour() throws Exception {
        CapturingLauncher launcher = new CapturingLauncher(
                "{\"status\":\"completed\",\"dry_run\":false}");
        OfflineRiskProductionJob job = new OfflineRiskProductionJob(
                new OfflineRiskProductionRunner(launcher));

        job.execute("{}", SCHEDULED_AT);

        assertThat(launcher.environment.get("OFFLINE_AS_OF"))
                .isEqualTo("2026-11-13T11:00:00Z");
    }

    @Test
    void shadowKeepsExplicitBackfillTimeAheadOfScheduledTime() throws Exception {
        CapturingLauncher launcher = new CapturingLauncher(
                "{\"status\":\"preview\",\"dry_run\":true}");
        OfflineRiskShadowJob job = new OfflineRiskShadowJob(
                new OfflineRiskPreviewRunner(launcher));

        job.execute("{\"as_of\":\"2026-11-12T08:00:00Z\"}", SCHEDULED_AT);

        assertThat(launcher.environment.get("OFFLINE_AS_OF"))
                .isEqualTo("2026-11-12T08:00:00Z");
    }

    private static final class CapturingLauncher implements FixedProcessLauncher {
        private final String output;
        private Map<String, String> environment;

        private CapturingLauncher(String output) {
            this.output = output;
        }

        @Override
        public ProcessOutput launch(
                List<String> command,
                Map<String, String> environment,
                Duration timeout) {
            this.environment = environment;
            return new ProcessOutput(0, output);
        }
    }
}
