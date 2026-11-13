#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MODULE_DIR="$ROOT_DIR/xxl-job-executor-samples/xxl-job-executor-risk-shadow"
HANDLER_FILE="$MODULE_DIR/src/main/java/com/xxl/job/executor/riskshadow/RiskShadowJobHandlers.java"
RUNNER_FILE="$MODULE_DIR/src/main/java/com/xxl/job/executor/riskshadow/OfflineRiskPreviewRunner.java"
PRODUCTION_RUNNER_FILE="$MODULE_DIR/src/main/java/com/xxl/job/executor/riskshadow/OfflineRiskProductionRunner.java"
LAUNCHER_FILE="$MODULE_DIR/src/main/java/com/xxl/job/executor/riskshadow/JvmFixedProcessLauncher.java"
CONFIG_FILE="$ROOT_DIR/xxl-job-executor-samples/xxl-job-executor-risk-shadow/src/main/resources/application.properties"

fail() {
  printf 'FAIL: %s\n' "$*" >&2
  exit 1
}

[ -f "$HANDLER_FILE" ] || fail "missing restricted risk shadow handler"
[ -f "$RUNNER_FILE" ] || fail "missing fixed offline preview runner"
[ -f "$PRODUCTION_RUNNER_FILE" ] || fail "missing fixed offline production runner"
[ -f "$LAUNCHER_FILE" ] || fail "missing fixed process launcher"
[ -f "$CONFIG_FILE" ] || fail "missing restricted executor configuration"

handlers="$(sed -n 's/.*@XxlJob("\([^"]*\)").*/\1/p' "$HANDLER_FILE" | sort)"
expected="$(printf '%s\n' \
  offlineRiskScanShadow \
  offlineRiskScan \
  riskFeatureSnapshotShadow \
  riskWarehouseCompactionShadow | sort)"

[ "$handlers" = "$expected" ] || fail "executor must expose only the four approved handlers"
grep -Fqx 'xxl.job.executor.glueenabled=false' "$CONFIG_FILE" || fail "GLUE execution must be disabled"

if grep -REq 'Runtime\.getRuntime|commandJobHandler|httpJobHandler' "$MODULE_DIR/src/main/java"; then
  fail "restricted executor must not expose arbitrary command or HTTP execution"
fi

process_builder_files="$(grep -Rl 'ProcessBuilder' "$MODULE_DIR/src/main/java" || true)"
[ "$process_builder_files" = "$LAUNCHER_FILE" ] || fail "ProcessBuilder is allowed only in the fixed launcher"
grep -Fq 'new ProcessBuilder(command)' "$LAUNCHER_FILE" || fail "fixed launcher must receive a typed command list"
grep -Fq 'List.of("/offline-risk")' "$RUNNER_FILE" || fail "offline preview command must be fixed"
grep -Fq 'List.of("/offline-risk")' "$PRODUCTION_RUNNER_FILE" || fail "offline production command must be fixed"
grep -Fq '"OFFLINE_DRY_RUN", "false"' "$PRODUCTION_RUNNER_FILE" || fail "production runner must explicitly disable dry-run mode"

printf 'PASS: restricted risk shadow executor contract\n'
