package com.xxl.job.executor.riskshadow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchedulerOverlapProbeRequestTest {
    @Test
    void acceptsARestrictedProbeRequest() {
        SchedulerOverlapProbeRequest request = SchedulerOverlapProbeRequest.parse(
                "{\"probe_id\":\"serial-01\",\"sleep_seconds\":12}");

        assertThat(request.probeId()).isEqualTo("serial-01");
        assertThat(request.sleepSeconds()).isEqualTo(12);
    }

    @Test
    void rejectsUnknownFieldsAndUnsafeDurations() {
        assertThatThrownBy(() -> SchedulerOverlapProbeRequest.parse(
                "{\"probe_id\":\"p1\",\"sleep_seconds\":121}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 120");

        assertThatThrownBy(() -> SchedulerOverlapProbeRequest.parse(
                "{\"probe_id\":\"p1\",\"sleep_seconds\":5,\"command\":\"rm\"}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsupported field");
    }
}
