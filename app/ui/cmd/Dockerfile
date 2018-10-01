FROM golang:1.11.0
WORKDIR /src
COPY . /src
RUN go mod vendor
RUN CGO_ENABLED=0 go build -mod=vendor -o /syndesis-ui *.go
