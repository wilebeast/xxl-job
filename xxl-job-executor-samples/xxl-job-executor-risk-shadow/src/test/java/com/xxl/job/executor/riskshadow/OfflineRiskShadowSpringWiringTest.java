package com.xxl.job.executor.riskshadow;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class OfflineRiskShadowSpringWiringTest {
    @Test
    void createsTheProductionShadowJobGraph() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(
                    JvmFixedProcessLauncher.class,
                    OfflineRiskPreviewRunner.class,
                    OfflineRiskProductionRunner.class,
                    OfflineRiskShadowJob.class,
                    OfflineRiskProductionJob.class,
                    RiskShadowJobHandlers.class);
            context.refresh();

            assertThat(context.getBean(RiskShadowJobHandlers.class)).isNotNull();
        }
    }
}
