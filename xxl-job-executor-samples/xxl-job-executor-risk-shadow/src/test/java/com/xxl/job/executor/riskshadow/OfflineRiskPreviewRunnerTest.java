package com.xxl.job.executor.riskshadow;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OfflineRiskPreviewRunnerTest {
    @Test
    void invokesOnlyTheFixedOfflinePreviewCommand() throws Exception {
        CapturingLauncher launcher = new CapturingLauncher();
        OfflineRiskPreviewRunner runner = new OfflineRiskPreviewRunner(launcher);
        Instant asOf = Instant.parse("2026-11-13T11:00:00Z");

        String output = runner.preview(asOf);

        assertThat(launcher.command).containsExactly("/offline-risk");
        assertThat(launcher.environment).containsExactlyInAnyOrderEntriesOf(Map.of(
                "OFFLINE_DRY_RUN", "true",
                "OFFLINE_AS_OF", "2026-11-13T11:00:00Z",
                "OFFLINE_RUN_ID", "xxl-shadow-20261113T110000Z"));
        assertThat(launcher.timeout).isEqualTo(Duration.ofSeconds(60));
        assertThat(output).isEqualTo("{\"status\":\"preview\",\"dry_run\":true}");
    }

    private static final class CapturingLauncher implements FixedProcessLauncher {
        private List<String> command;
        private Map<String, String> environment;
        private Duration timeout;

        @Override
        public ProcessOutput launch(
                List<String> command,
                Map<String, String> environment,
                Duration timeout) {
            this.command = command;
            this.environment = environment;
            this.timeout = timeout;
            return new ProcessOutput(0, "{\"status\":\"preview\",\"dry_run\":true}");
        }
    }
}
