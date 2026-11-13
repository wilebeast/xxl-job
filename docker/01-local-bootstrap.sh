#!/usr/bin/env bash
set -euo pipefail

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}"
: "${XXL_JOB_ADMIN_PASSWORD:?XXL_JOB_ADMIN_PASSWORD is required}"
: "${XXL_JOB_EXECUTOR_ACCESS_TOKEN:?XXL_JOB_EXECUTOR_ACCESS_TOKEN is required}"

mysql=(mysql --protocol=socket -uroot -p"$MYSQL_ROOT_PASSWORD" xxl_job)

"${mysql[@]}" <<SQL
UPDATE xxl_job_user
SET password = SHA2('${XXL_JOB_ADMIN_PASSWORD}', 256), token = NULL
WHERE username = 'admin';

DELETE FROM xxl_job_info;
DELETE FROM xxl_job_group;

INSERT INTO xxl_job_group
  (id, app_name, name, address_type, address_list, access_token, update_time)
VALUES
  (1, 'risk-shadow-executor', 'Risk Shadow Executor', 0, NULL,
   '${XXL_JOB_EXECUTOR_ACCESS_TOKEN}', NOW());

INSERT INTO xxl_job_info
  (id, job_group, name, author, alarm_email, schedule_type, schedule_conf,
   misfire_strategy, executor_route_strategy, executor_handler, executor_param,
   executor_block_strategy, executor_timeout, executor_fail_retry_count,
   glue_type, glue_source, glue_remark, glue_updatetime, child_jobid,
   trigger_status, trigger_last_time, trigger_next_time, add_time, update_time)
VALUES
  (1, 1, 'Warehouse compaction shadow', 'local-platform', '', 'NONE', '',
   'DO_NOTHING', 'FIRST', 'riskWarehouseCompactionShadow',
   '{"shadow_only":true,"source":"kubernetes-cronjob"}',
   'SERIAL_EXECUTION', 60, 0, 'BEAN', '', 'restricted shadow handler', NOW(), '',
   0, 0, 0, NOW(), NOW()),
  (2, 1, 'Feature snapshot shadow', 'local-platform', '', 'NONE', '',
   'DO_NOTHING', 'FIRST', 'riskFeatureSnapshotShadow',
   '{"shadow_only":true,"source":"kubernetes-cronjob"}',
   'SERIAL_EXECUTION', 60, 0, 'BEAN', '', 'restricted shadow handler', NOW(), '',
   0, 0, 0, NOW(), NOW()),
  (3, 1, 'Offline risk scan shadow', 'local-platform', '', 'CRON', '0 5 * * * ? *',
   'DO_NOTHING', 'FIRST', 'offlineRiskScanShadow',
   '{}',
   'SERIAL_EXECUTION', 60, 0, 'BEAN', '', 'restricted shadow handler', NOW(), '',
   0, 0, 0, NOW(), NOW()),
  (4, 1, 'Offline risk scan production', 'local-platform', '', 'CRON', '0 5 * * * ? *',
   'DO_NOTHING', 'FIRST', 'offlineRiskScan',
   '{}',
   'SERIAL_EXECUTION', 60, 0, 'BEAN', '', 'restricted production handler', NOW(), '',
   0, 0, 0, NOW(), NOW());
SQL

printf 'XXL-JOB local schema initialized with three Shadow jobs and one stopped production job.\n'
