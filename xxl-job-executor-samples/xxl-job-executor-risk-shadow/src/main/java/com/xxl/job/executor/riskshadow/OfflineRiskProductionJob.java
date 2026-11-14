package com.xxl.job.executor.riskshadow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class OfflineRiskProductionJob {
    private final OfflineRiskProductionRunner runner;
    private final Clock clock;

    @Autowired
    public OfflineRiskProductionJob(OfflineRiskProductionRunner runner) {
        this(runner, Clock.systemUTC());
    }

    OfflineRiskProductionJob(OfflineRiskProductionRunner runner, Clock clock) {
        this.runner = runner;
        this.clock = clock;
    }

    public ProductionExecution execute(String rawParameter) throws Exception {
        return execute(rawParameter, clock.instant());
    }

    public ProductionExecution execute(String rawParameter, Instant scheduledAt) throws Exception {
        OfflineRiskShadowRequest request = OfflineRiskShadowRequest.parse(rawParameter, scheduledAt);
        return new ProductionExecution(request.asOf().toString(), runner.run(request.asOf()));
    }

    public record ProductionExecution(String asOf, String output) {
    }
}
