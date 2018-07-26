### Go Development environment

Use this image to compile with Go 1.10 or update you dependencies, which is useful especially when building the syndesis operator.


#### dep ensure

```
docker run -w /gopath/src/github.com/syndesisio/syndesis/install/operator \
           -v $GOPATH/pkg/dep:/gopath/pkg/dep \
           -v $SYNDESIS_TOP_DIR:/gopath/src/github.com/syndesisio/syndesis \
           syndesis/godev:1.10 \
           dep ensure
```

#### go build

```
docker run -w /gopath/src/github.com/syndesisio/syndesis/install/operator \
           -v $SYNDESIS_TOP_DIR:/gopath/src/github.com/syndesisio/syndesis \
           syndesis/godev:1.10 \
           go build ./cmd/syndesis-operator
```

