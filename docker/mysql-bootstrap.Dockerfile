FROM mysql:8.4
COPY doc/db/tables_xxl_job.sql /docker-entrypoint-initdb.d/00-tables_xxl_job.sql
COPY docker/01-local-bootstrap.sh /docker-entrypoint-initdb.d/01-local-bootstrap.sh
RUN chmod 0755 /docker-entrypoint-initdb.d/01-local-bootstrap.sh
