#
# This Makefile is only used to speed up local development.
# Depends on make and go 1.11.  Not used during mvn based builds,
# mvn will call ./build.sh instead which only depends on docker.
#

# To enable debug:
#BUILD_FLAGS=-gcflags "all=-N -l"

all:
	go build $(BUILD_FLAGS) -o ../target/syndesis-ui *.go
	@echo
	@echo =====================================================
	@echo
	@echo   You can run the server locally with:
	@echo   ../target/syndesis-ui --path ../dist --port 8080 --spa --etags
	@echo
	@echo   To update the image, run: make image
	@echo
	@echo =====================================================
	@echo

linux: ../target/go/linux-amd64/syndesis-ui
../target/go/linux-amd64/syndesis-ui: Makefile *.go
	GOOS=linux GOARCH=amd64 go build $(BUILD_FLAGS) -o $@ *.go

image: linux
	@echo
	@echo =====================================================
	@echo
	@echo   Rebuilding the image, this assumes you have have
	@echo   already run yarn build and have a valid dist dir.
	@echo
	@echo =====================================================
	@echo
	cd ..; mvn fabric8:build -P image
