## Syndesis

[![Latest Release](https://img.shields.io/github/v/release/syndesisio/syndesis)](https://github.com/syndesisio/syndesis/releases/latest)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.syndesis/syndesis-parent/badge.svg?style=flat-square)](https://search.maven.org/search?q=g:io.syndesis)
[![CircleCI](https://circleci.com/gh/syndesisio/syndesis/tree/master.svg?style=svg)](https://circleci.com/gh/syndesisio/syndesis/tree/master)
[![Gitter](https://badges.gitter.im/syndesisio/community.svg)](https://gitter.im/syndesisio/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Google Groups](https://img.shields.io/badge/Google%20Groups-Syndesis-blue)](https://groups.google.com/forum/#!forum/syndesis)

A flexible and customizable, open source platform that provides core integration capabilities as a service.

All developer related documentation can be found at the [Development QuickStart](https://syndesis.io/docs/development_quickstart/), read indepth about the tooling to help with development in the [`syndesis` CLI](https://syndesis.io/docs/cli/syndesis/) documentation.

### Quickstart

* To get started quickly please install [Minishift](https://www.okd.io/minishift/) first.
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
