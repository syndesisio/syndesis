#!/bin/env bash

set -eo pipefail

appdir=$(cd "$(dirname "${BASH_SOURCE[0]}")/../../app" && pwd)

if [ "$#" -eq 0 ]; then
  cd $appdir && ./mvnw -q -B -N license:format
else
  TMPPOM=$(mktemp -p $appdir pom-format-XXXXXX.xml)
  function cleanup {
    rm -f "$TMPPOM"
  }
  trap cleanup EXIT
  cat << POM > "$TMPPOM"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>io.syndesis</groupId>
    <artifactId>syndesis-parent</artifactId>
    <version>$(grep -oPm2 "(?<=<version>)[^<]+" "$appdir/pom.xml"|tail -1)</version>
    <relativePath>.</relativePath>
  </parent>
  <artifactId>tmp</artifactId>
  <build>
    <plugins>
      <plugin>
        <groupId>com.mycila</groupId>
        <artifactId>license-maven-plugin</artifactId>
        <configuration>
          <includes>
            $(for f in "$@"; do echo "<include>${f#$appdir/}</include>"; done)
          </includes>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
POM

  cd $appdir && ./mvnw -q -B -N -f "$TMPPOM" license:format
fi
