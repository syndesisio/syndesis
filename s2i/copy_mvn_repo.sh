#!/bin/bash

VERSION="$1"
TARGET_DIR="$2"

XML="""
  <distributionManagement>
    <snapshotRepository>
      <id>local</id>
      <url>file:/deployments/project/.m2/repository</url>
    </snapshotRepository>
    <repository>
      <id>local</id>
      <url>file:/deployments/project/.m2/repository</url>
    </repository>
  </distributionManagement>
"""
perl -p -i -e "s|</project>|${XML}</project>|" "$TARGET_DIR/pom.xml"

mkdir -p "$TARGET_DIR/.m2/repository"
cd ~/.m2/repository
find ./io/syndesis | grep -F -- "/${VERSION}/" | xargs tar -c | tar -x -C "$TARGET_DIR/.m2/repository"

