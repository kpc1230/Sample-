#!/usr/bin/env bash
set -e

echo "Postgres version ${PG_MAJOR}"
echo "Internal data folder is ${PGDATA}"
echo "Using additional configuration from '/etc/postgresql/conf.d/' folder "

# Make postgres generally configurable by including a conf.d folder
# Info: parameter was introduced in postgres version 9.3
sed -i -e "s|^#include_dir =.*$|include_dir = '/etc/postgresql/conf.d/'|" ${PGDATA}/postgresql.conf
