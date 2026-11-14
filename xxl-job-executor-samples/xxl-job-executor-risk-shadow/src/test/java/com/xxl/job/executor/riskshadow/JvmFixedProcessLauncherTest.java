package com.xxl.job.executor.riskshadow;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class JvmFixedProcessLauncherTest {
    @Test
    void interruptionTerminatesTheStartedProcess() throws Exception {
        Path pidFile = Files.createTempFile("xxl-process", ".pid");
        Files.deleteIfExists(pidFile);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> launch = executor.submit(() -> {
            try {
                new JvmFixedProcessLauncher().launch(
                        List.of("/bin/sh", "-c", "echo $$ > '" + pidFile + "'; exec sleep 30"),
                        Map.of(),
                        Duration.ofMinutes(1));
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
        });

        long pid = waitForPid(pidFile);
        ProcessHandle process = ProcessHandle.of(pid).orElseThrow();
        try {
            launch.cancel(true);
            assertThat(waitUntilStopped(process, Duration.ofSeconds(2))).isTrue();
        } finally {
            process.destroyForcibly();
            executor.shutdownNow();
            Files.deleteIfExists(pidFile);
        }
    }

    private static long waitForPid(Path pidFile) throws Exception {
        for (int attempt = 0; attempt < 100; attempt++) {
            if (Files.exists(pidFile)) {
                return Long.parseLong(Files.readString(pidFile).trim());
            }
            Thread.sleep(20);
        }
        throw new IllegalStateException("process did not write its pid");
    }

    private static boolean waitUntilStopped(ProcessHandle process, Duration timeout)
            throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (process.isAlive() && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        return !process.isAlive();
    }
}
