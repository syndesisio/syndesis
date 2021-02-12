## Syndesis

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.syndesis/syndesis-parent/badge.svg?style=flat-square)](https://search.maven.org/search?q=g:io.syndesis)
[![CircleCI](https://circleci.com/gh/syndesisio/syndesis/tree/master.svg?style=svg)](https://circleci.com/gh/syndesisio/syndesis/tree/master)
[![Gitter](https://badges.gitter.im/syndesisio/community.svg)](https://gitter.im/syndesisio/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=syndesis-parent&metric=alert_status)](https://sonarcloud.io/dashboard?id=syndesis-parent)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=syndesis-parent&metric=coverage)](https://sonarcloud.io/dashboard?id=syndesis-parent)



A flexible and customizable, open source platform that provides core integration capabilities as a service.

All developer related documentation can be found at the [Syndesis Developer Handbook](https://doc.syndesis.io).

### Quickstart

* To get started quickly please install [Minishift](https://www.openshift.org/minishift/) first.
* Clone this repository and enter it:

```
git clone https://github.com/syndesisio/syndesis.git
cd syndesis
```

* Startup minishift and install:

```
./tools/bin/syndesis minishift --install --open --nodev
```

This will install the latest bleeding edge version from Syndesis from the `master` branch.
For a more stable experience, use the option `--tag` with a [stable version](https://github.com/syndesisio/syndesis/releases).

Now you can run some [quickstarts](https://github.com/syndesisio/syndesis-quickstarts/blob/master/README.md#4-lets-run-some-quickstarts)
