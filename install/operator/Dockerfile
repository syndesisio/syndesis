FROM golang:1.12.0
#RUN go get -u github.com/golang/dep/cmd/dep
WORKDIR /go/src/github.com/syndesisio/syndesis/install/operator
ENV GO111MODULE=on
COPY go.mod .
COPY go.sum .
RUN go mod download
COPY . .
RUN go mod vendor
RUN go build -o /operator -gcflags all=-trimpath=${GOPATH} -asmflags all=-trimpath=${GOPATH} -mod=vendor github.com/syndesisio/syndesis/install/operator/cmd/manager
