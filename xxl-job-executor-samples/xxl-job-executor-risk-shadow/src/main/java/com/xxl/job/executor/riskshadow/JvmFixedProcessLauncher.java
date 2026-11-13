package com.xxl.job.executor.riskshadow;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class JvmFixedProcessLauncher implements FixedProcessLauncher {
    @Override
    public ProcessOutput launch(
            List<String> command,
            Map<String, String> environment,
            Duration timeout) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().putAll(environment);
        Process process = builder.start();
        CompletableFuture<String> output = CompletableFuture.supplyAsync(() -> {
            try {
                return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception error) {
                throw new IllegalStateException("read fixed process output", error);
            }
        });
        if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("fixed offline preview timed out");
        }
        return new ProcessOutput(process.exitValue(), output.get(5, TimeUnit.SECONDS).trim());
    }
}
