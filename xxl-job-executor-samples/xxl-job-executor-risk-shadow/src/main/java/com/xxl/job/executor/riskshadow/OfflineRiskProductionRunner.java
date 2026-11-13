package com.xxl.job.executor.riskshadow;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class OfflineRiskProductionRunner {
    private static final List<String> COMMAND = List.of("/offline-risk");
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final FixedProcessLauncher launcher;

    public OfflineRiskProductionRunner(FixedProcessLauncher launcher) {
        this.launcher = launcher;
    }

    public String run(Instant asOf) throws Exception {
        if (asOf == null) {
            throw new IllegalArgumentException("as_of is required");
        }
        ProcessOutput result = launcher.launch(
                COMMAND,
                Map.of(
                        "OFFLINE_DRY_RUN", "false",
                        "OFFLINE_AS_OF", asOf.toString()),
                TIMEOUT);
        if (result.exitCode() != 0) {
            throw new IllegalStateException(
                    "offline execution failed with exit code " + result.exitCode() + ": " + result.output());
        }
        if (!result.output().contains("\"dry_run\":false")
                || !result.output().contains("\"status\":\"completed\"")) {
            throw new IllegalStateException("offline execution returned an invalid response");
        }
        return result.output();
    }
}
