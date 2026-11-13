package com.xxl.job.executor.riskshadow;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

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
                    offlineRiskShadowJob.execute(XxlJobHelper.getJobParam());
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
                    offlineRiskProductionJob.execute(XxlJobHelper.getJobParam());
            XxlJobHelper.log(
                    "production=true task_key=offline-risk-scan as_of={} result={}",
                    execution.asOf(), execution.output());
            XxlJobHelper.handleSuccess("offline execution completed");
        } catch (Exception error) {
            XxlJobHelper.handleFail("offline execution failed: " + error.getMessage());
            throw new IllegalStateException("offline risk execution failed", error);
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
}
