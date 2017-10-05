#!/bin/bash
# Builds a simple representive integration project so that all it's maven 
# dependencies get cached in the local ~/.m2/repository directory

cd /tmp/project
mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.2:tree
