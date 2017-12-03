# Syndesis Policy

This project holds the code style convention rules and license header for the Syndesis project.
See files below `src/main/resources` for the checkstyle and license definitions.

For updating those:

* Update rules
* Increase version number in `pom.xml`. These are integer versions like 1,2, ...
* Deploy to sonatype: `mvn -s maven-settings-with-sonatype-creds.yml clean install deploy`.
* Got to https://oss.sonatype.org/ and release.
* Update property `${syndesis.policy.version}` in the top-level Syndesis `pom.xml`
