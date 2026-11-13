package com.xxl.job.executor.riskshadow;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface FixedProcessLauncher {
    ProcessOutput launch(
            List<String> command,
            Map<String, String> environment,
            Duration timeout) throws Exception;
}
