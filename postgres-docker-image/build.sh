#!/usr/bin/env bash
docker rm -f capture-image && docker rmi -f docker.atlassian.io/capture/jira-cloud
docker build -t docker.atlassian.io/capture/jira-cloud . && docker run --publish 5432 --detach --name capture-image docker.atlassian.io/capture/jira-cloud
docker ps
