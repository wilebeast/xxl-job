package com.xxl.job.executor.riskshadow;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RiskShadowJobHandlersTest {
    @Test
    void exposesOnlyApprovedHandlers() {
        Set<String> handlers = Stream.of(RiskShadowJobHandlers.class.getDeclaredMethods())
                .map(method -> method.getAnnotation(XxlJob.class))
                .filter(annotation -> annotation != null)
                .map(XxlJob::value)
                .collect(Collectors.toSet());

        assertThat(handlers).containsExactlyInAnyOrder(
                "riskWarehouseCompactionShadow",
                "riskFeatureSnapshotShadow",
                "offlineRiskScanShadow",
                "offlineRiskScan",
                "schedulerOverlapProbe");
    }

    @Test
    void doesNotExposePrivateImplementationAsAJob() throws Exception {
        Method method = RiskShadowJobHandlers.class.getDeclaredMethod("recordShadowRun", String.class);
        assertThat(method.getAnnotation(XxlJob.class)).isNull();
    }
}
