package com.xxl.job.executor.riskshadow;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        try {
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("fixed offline process timed out");
            }
            return new ProcessOutput(process.exitValue(), output.get(5, TimeUnit.SECONDS).trim());
        } finally {
            terminateProcessTree(process);
        }
    }

    private static void terminateProcessTree(Process process) {
        List<ProcessHandle> descendants = process.descendants().collect(Collectors.toList());
        for (int index = descendants.size() - 1; index >= 0; index--) {
            ProcessHandle descendant = descendants.get(index);
            if (descendant.isAlive()) {
                descendant.destroyForcibly();
            }
        }
        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }
}
