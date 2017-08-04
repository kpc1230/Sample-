# JIRA Cloud Capture plugin Readme

This is Cloud Capture for JIRA plugin repository

Server plugin repository: https://stash.atlassian.com/projects/JCAP/repos/jira-capture/browse

Browser extensions repository: https://stash.atlassian.com/projects/JCAP/repos/jira-capture-browser-extensions/browse

## WELCOME
JIRA Capture is an addon to JIRA that allows rapid issue creation and provides test session management in support of exploratory testing.
JIRA Capture is formerly named Bonfire and was renamed at 26 Aug 2013 http://blogs.atlassian.com/2013/08/renaming-greenhopper-and-bonfire. Before that, it was known as Excalibur.

## PREREQUISITES

* Maven 3 to be used for project assembly.
* Docker must be installed

### Docker
Make sure that Docker is installed and running. If you are on OS X, use [Docker for Mac](https://www.docker.com/products/docker),
direct from the docker website, rather than docker machine or the outdated homebrew version.

Authenticate with Atlassian's Docker registry at docker.atlassian.io (use your Staff ID credentials, with Username, not email address):

    docker login docker.atlassian.io

#### Docker image for JIRA

Register / re-register the JIRA PostgreSQL image.

    docker rm -f capture-image 2>/dev/null;
    docker run --publish 5432 --detach --name capture-image docker.atlassian.io/capture/jira-cloud:1000.957.0

## BUILD
    mvn clean package -DskipTests

## RUNNING
Install POM changes:

    PROJECT_ROOT$ mvn clean install -DskipTests

Run in the plugin folder:

    PROJECT_ROOT/plugin$ mvn jira:debug -Dpostgres.port=$(docker port capture-image 5432 | cut -d: -f2)
    
Debug functional tests locally :

    PROJECT_ROOT/plugin-func-tests$ mvn jira:debug

NOTE: you can do jira:debug in the PROJECT_ROOT/plugin-func-tests$ and then atlas-cli in the PROJECT_ROOT/plugin folder
This allows you to run jira and pi your changes (use pi to call the Plugin Install command)

#RELEASE
* https://deployment-bamboo.internal.atlassian.com/browse/JCAP-JCAP

## DOCS
* https://extranet.atlassian.com/display/BON/JIRA+Capture+Development+-+Getting+Started
* https://extranet.atlassian.com/display/BON/JIRA+Capture+Builds
