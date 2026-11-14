package com.xxl.job.executor.riskshadow;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RiskShadowJobHandlers {
    private final OfflineRiskShadowJob offlineRiskShadowJob;
    private final OfflineRiskProductionJob offlineRiskProductionJob;

    public RiskShadowJobHandlers(
            OfflineRiskShadowJob offlineRiskShadowJob,
            OfflineRiskProductionJob offlineRiskProductionJob) {
        this.offlineRiskShadowJob = offlineRiskShadowJob;
        this.offlineRiskProductionJob = offlineRiskProductionJob;
    }

    @XxlJob("riskWarehouseCompactionShadow")
    public void riskWarehouseCompactionShadow() {
        recordShadowRun("risk-warehouse-compaction");
    }

    @XxlJob("riskFeatureSnapshotShadow")
    public void riskFeatureSnapshotShadow() {
        recordShadowRun("risk-feature-snapshot");
    }

    @XxlJob("offlineRiskScanShadow")
    public void offlineRiskScanShadow() {
        try {
            OfflineRiskShadowJob.PreviewExecution execution =
                    offlineRiskShadowJob.execute(XxlJobHelper.getJobParam(), scheduledAt());
            XxlJobHelper.log(
                    "shadow_only=true task_key=offline-risk-scan as_of={} preview={}",
                    execution.asOf(), execution.output());
            XxlJobHelper.handleSuccess("offline preview completed; no business side effects");
        } catch (Exception error) {
            XxlJobHelper.handleFail("offline preview failed: " + error.getMessage());
            throw new IllegalStateException("offline risk shadow failed", error);
        }
    }

    @XxlJob("offlineRiskScan")
    public void offlineRiskScan() {
        try {
            OfflineRiskProductionJob.ProductionExecution execution =
                    offlineRiskProductionJob.execute(XxlJobHelper.getJobParam(), scheduledAt());
            XxlJobHelper.log(
                    "production=true task_key=offline-risk-scan as_of={} result={}",
                    execution.asOf(), execution.output());
            XxlJobHelper.handleSuccess("offline execution completed");
        } catch (Exception error) {
            XxlJobHelper.handleFail("offline execution failed: " + error.getMessage());
            throw new IllegalStateException("offline risk execution failed", error);
        }
    }

    @XxlJob("schedulerOverlapProbe")
    public void schedulerOverlapProbe() {
        SchedulerOverlapProbeRequest request =
                SchedulerOverlapProbeRequest.parse(XxlJobHelper.getJobParam());
        XxlJobHelper.log(
                "probe_event=start probe_id={} sleep_seconds={} thread={}",
                request.probeId(), request.sleepSeconds(), Thread.currentThread().getName());
        try {
            Thread.sleep(request.sleepSeconds() * 1000L);
            XxlJobHelper.log("probe_event=completed probe_id={}", request.probeId());
            XxlJobHelper.handleSuccess("scheduler overlap probe completed");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            XxlJobHelper.log("probe_event=interrupted probe_id={}", request.probeId());
            XxlJobHelper.handleFail("scheduler overlap probe interrupted");
            throw new IllegalStateException("scheduler overlap probe interrupted", error);
        }
    }

    private void recordShadowRun(String taskKey) {
        String parameter = XxlJobHelper.getJobParam();
        XxlJobHelper.log(
                "shadow_only=true task_key={} parameter={}",
                taskKey,
                parameter == null ? "" : parameter);
        XxlJobHelper.handleSuccess("shadow run recorded; no business side effects");
    }

    private Instant scheduledAt() {
        long logDateTime = XxlJobHelper.getLogDateTime();
        return logDateTime > 0 ? Instant.ofEpochMilli(logDateTime) : Instant.now();
    }
}
