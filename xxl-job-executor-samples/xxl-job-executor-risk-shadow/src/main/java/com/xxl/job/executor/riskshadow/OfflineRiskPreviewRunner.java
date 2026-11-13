package com.xxl.job.executor.riskshadow;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class OfflineRiskPreviewRunner {
    private static final List<String> COMMAND = List.of("/offline-risk");
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final DateTimeFormatter RUN_ID_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final FixedProcessLauncher launcher;

    public OfflineRiskPreviewRunner(FixedProcessLauncher launcher) {
        this.launcher = launcher;
    }

    public String preview(Instant asOf) throws Exception {
        if (asOf == null) {
            throw new IllegalArgumentException("as_of is required");
        }
        ProcessOutput result = launcher.launch(
                COMMAND,
                Map.of(
                        "OFFLINE_DRY_RUN", "true",
                        "OFFLINE_AS_OF", asOf.toString(),
                        "OFFLINE_RUN_ID", "xxl-shadow-" + RUN_ID_TIME.format(asOf)),
                TIMEOUT);
        if (result.exitCode() != 0) {
            throw new IllegalStateException(
                    "offline preview failed with exit code " + result.exitCode() + ": " + result.output());
        }
        if (!result.output().contains("\"dry_run\":true") || !result.output().contains("\"status\":\"preview\"")) {
            throw new IllegalStateException("offline preview returned a non-preview response");
        }
        return result.output();
    }
}
