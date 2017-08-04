#!/usr/bin/env bash

# when running integration tests maven-jira-plugin by default creates 'jira' database and 'jira' user, so need to check if they exists before creating them
psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname='jira'" | grep -q 1 || psql -U postgres -c "CREATE USER jira WITH NOSUPERUSER INHERIT NOCREATEROLE NOCREATEDB LOGIN PASSWORD 'jira'"
psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'jira'" | grep -q 1 || psql -U postgres -c "CREATE DATABASE jira WITH OWNER jira ENCODING UNICODE"

psql -U postgres -tc "CREATE USER jira_export WITH NOSUPERUSER INHERIT NOCREATEROLE NOCREATEDB LOGIN PASSWORD 'jira_export'"
psql -U postgres -tc "CREATE DATABASE jira_export WITH OWNER jira_export ENCODING UNICODE"
