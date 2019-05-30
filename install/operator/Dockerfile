FROM golang:1.12.0
WORKDIR /go/src/github.com/syndesisio/syndesis/install/operator
ENV GO111MODULE=on

# This will speed up subsequent builds if the go deps don't change due thx to image layer caching.
# Note: the vendor dir is in .dockerignore file so that the build context sent to docker is really small.
COPY go.mod .
COPY go.sum .
RUN go mod download

COPY . .
RUN go mod vendor
RUN go test ./cmd/... ./pkg/...
RUN go build -o /operator -gcflags all=-trimpath=${GOPATH} -asmflags all=-trimpath=${GOPATH} -mod=vendor github.com/syndesisio/syndesis/install/operator/cmd/manager
