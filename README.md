# Action Engine
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.springframework.boot/spring-boot-starter-parent/badge.svg)](https://search.maven.org/artifact/org.springframework.boot/spring-boot-starter-parent)
[![Docker Hub](https://img.shields.io/docker/pulls/airta/actionengine.svg?style=flat)](https://cloud.docker.com/u/airta/repository/docker/airta/actionengine/)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/allenyinx/actionEngine.svg)](http://isitmaintained.com/project/allenyinx/actionEngine "Average time to resolve an issue")
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=allenyinx_actionEngine&metric=bugs)](https://sonarcloud.io/dashboard?id=allenyinx_actionEngine)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=allenyinx_actionEngine&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=allenyinx_actionEngine)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=allenyinx_actionEngine&metric=code_smells)](https://sonarcloud.io/dashboard?id=allenyinx_actionEngine)

<p align="left">
    <a href="https://codecov.io/gh/allenyinx/actionEngine"><img src="https://codecov.io/gh/allenyinx/actionEngine/branch/develop/graph/badge.svg" /></a>
    <a href='https://circleci.com/gh/allenyinx/actionEngine/tree/develop'><img src='https://circleci.com/gh/allenyinx/actionEngine/tree/develop.svg?style=svg'></a>
    <a href='https://sonarcloud.io/dashboard?id=allenyinx_ActionAgent'><img src='https://sonarcloud.io/api/project_badges/measure?project=allenyinx_ActionAgent&metric=alert_status'></a>
    <a href='https://travis-ci.org/allenyinx/actionEngine'><img src='https://travis-ci.org/allenyinx/actionEngine.svg?branch=develop'></a>
    <a href='http://52.175.51.58:8080/job/ActionEngine/'><img src='http://52.175.51.58:8080/buildStatus/icon?job=ActionEngine'></a>
</p>

# action engine
Engine for schedule, manage action agent.


## Features
* Listen for Flow Script messages
* Parsing flow scripts to action list
* Produce action messages for agent to consume and execute
* Sitemap store, update, present
* Agent Pool
* Initiate and schedule Action Agent

Launch
=======

The quick way:

    docker run airta/actionengine:latest
    kubectl apply -f deployment/deploy_engine.yaml

