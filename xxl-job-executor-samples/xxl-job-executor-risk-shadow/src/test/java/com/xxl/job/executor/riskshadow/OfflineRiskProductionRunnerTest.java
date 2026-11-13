package com.xxl.job.executor.riskshadow;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OfflineRiskProductionRunnerTest {
    @Test
    void invokesOnlyTheFixedPersistentOfflineCommand() throws Exception {
        CapturingLauncher launcher = new CapturingLauncher();
        OfflineRiskProductionRunner runner = new OfflineRiskProductionRunner(launcher);

        String output = runner.run(Instant.parse("2026-11-13T15:00:00Z"));

        assertThat(launcher.command).containsExactly("/offline-risk");
        assertThat(launcher.environment).containsExactlyInAnyOrderEntriesOf(Map.of(
                "OFFLINE_DRY_RUN", "false",
                "OFFLINE_AS_OF", "2026-11-13T15:00:00Z"));
        assertThat(launcher.timeout).isEqualTo(Duration.ofSeconds(60));
        assertThat(output).isEqualTo(
                "{\"status\":\"completed\",\"dry_run\":false,\"idempotent_replay\":true}");
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
            return new ProcessOutput(
                    0,
                    "{\"status\":\"completed\",\"dry_run\":false,\"idempotent_replay\":true}");
        }
    }
}
