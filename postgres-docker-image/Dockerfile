FROM postgres:9.5
MAINTAINER JIRA Cloud

# set default postgres user
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres

COPY conf.d/ci-perf-tweaks.conf /etc/postgresql/conf.d/10-ci-perf-tweaks.conf
COPY setup/ci-enable-include_dir.sh /docker-entrypoint-initdb.d/10-ci-enable-include_dir.sh


# create default databases only
COPY sql/ci-default-databases.sh /docker-entrypoint-initdb.d/15-ci-default-databases.sh

# create default jira schema (dump from JIRA with sample project, issue and test session)
COPY sql/init-db.sql /docker-entrypoint-initdb.d/20-initial-db.sql

# update license for one with Capture
COPY sql/swap-license.sql /docker-entrypoint-initdb.d/30-swap-license.sql