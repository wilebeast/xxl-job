package com.xxl.job.executor.riskshadow;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Instant;

@Component
public class OfflineRiskShadowJob {
    private final OfflineRiskPreviewRunner runner;
    private final Clock clock;

    @Autowired
    public OfflineRiskShadowJob(OfflineRiskPreviewRunner runner) {
        this(runner, Clock.systemUTC());
    }

    OfflineRiskShadowJob(OfflineRiskPreviewRunner runner, Clock clock) {
        this.runner = runner;
        this.clock = clock;
    }

    public PreviewExecution execute(String rawParameter) throws Exception {
        return execute(rawParameter, clock.instant());
    }

    public PreviewExecution execute(String rawParameter, Instant scheduledAt) throws Exception {
        OfflineRiskShadowRequest request = OfflineRiskShadowRequest.parse(rawParameter, scheduledAt);
        return new PreviewExecution(request.asOf().toString(), runner.preview(request.asOf()));
    }

    public record PreviewExecution(String asOf, String output) {
    }
}
