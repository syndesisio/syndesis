#!/usr/bin/env bash
#
# Some reusable shell functions:
#

__ARGS__=("$@")

# Outputs the value of a command line option.
# Usage: readopt <flag> <default value>
# Example: kind=$(readopt --kind MyKind)
readopt() {
    local filter="$1"
    local default="$2"
    local next=false
    for var in "${__ARGS__[@]}"; do
        if $next; then
            local value="${var##-}"
            if [ "$value" != "$var" ]; then
               # Next is already also option, so assume it's being used like a flag
               echo true
               return
            fi
            echo $var
            return
        fi
        if [[ "$var" = "${filter}"* ]]; then
            local value="${var//${filter}=/}"
            if [ "$value" != "$var" ]; then
                echo $value
                return
            fi
            next=true
        fi
    done
    if $next; then
       # Ran out of options treat like a flag
       echo true
    fi

    echo $default
}

build_operator()
{
    local strategy="$1"
    shift
    local source_gen="$1"
    shift
    local go_proxy_url="$1"
    shift

    local hasgo=$(go_is_available)
    local hasdocker=$(docker_is_available)

    if [ "$strategy" == "auto" ] ; then
        if [ "$hasgo" == "OK" ]; then
            strategy="go"
        elif [ "$hasdocker" == "OK" ]; then
            # go not available so try docker
            printf "WARN: Building with 'docker' since 'go' command is not available ... \n\t\t$hasgo\n"
            strategy="docker"
        else
            echo "ERROR: Building the operator requires either 'docker' or 'go' commands to be installed."
            exit 1
        fi
    fi

    case "$strategy" in
    "go")
        if [ "$hasgo" != "OK" ]; then
            echo "$hasgo"
            exit 1
        fi

        echo ======================================================
        echo Building executable with go tooling
        echo ======================================================
        export GO111MODULE=on
        export GOPROXY="$go_proxy_url"

        if [[ ( "$source_gen" == "on" ) || ( "$source_gen" == "verify-none" ) ]]; then
        	echo "generating sources"
	        go mod vendor

	        local hassdk=$(operatorsdk_is_available)
	        if [ "$hassdk" == "OK" ]; then
	            operator-sdk generate k8s
	            operator-sdk generate crds
	        else
	            # display warning message and move on
	            printf "$hassdk\n\n"
	        fi

	        openapi_gen
	        go generate ./pkg/...
    	    go mod tidy

            if [ "$source_gen" == "verify-none" ]; then
        	    echo "verifying no sources have been generated"
        	    for file in pkg/apis/syndesis/v1beta1/zz_generated.deepcopy.go pkg/generator/assets_vfsdata.go; do
                    if [ "$(git diff $file)" != "" ] ; then
                        echo ===========================================
                        echo   Looks like some generated source code
                        echo   not previously checked in.  See diff:
                        echo ===========================================
                        echo
                        git diff $file
                        exit 1
                    fi
                done
            fi
        else
        	echo "skipping source generation"
        fi

        echo building executable
        go test -test.short -mod=vendor ./cmd/... ./pkg/...

        if [ -z "${GOOSLIST}" ]; then
            GOOSLIST="linux darwin windows"
        fi

        for GOARCH in amd64 ; do
          for GOOS in ${GOOSLIST} ; do
            export GOARCH GOOS
            echo building ./dist/${GOOS}-${GOARCH}/syndesis-operator executable
            go build  "$@" -o ./dist/${GOOS}-${GOARCH}/syndesis-operator \
                -gcflags all=-trimpath=${GOPATH} -asmflags all=-trimpath=${GOPATH} -mod=vendor \
                ./cmd/manager

            echo building ./dist/${GOOS}-${GOARCH}/platform-detect executable
            go build -o ./dist/${GOOS}-${GOARCH}/platform-detect \
                -gcflags all=-trimpath=${GOPATH} -asmflags all=-trimpath=${GOPATH} -mod=vendor \
                ./cmd/detect
          done
        done
        mkdir -p ./build/_output/bin
        cp ./dist/linux-amd64/syndesis-operator ./build/_output/bin/syndesis-operator

    ;;
    "docker")

        if [ "$hasdocker" != "OK" ]; then
            echo "$hasdocker"
            exit 1
        fi

        local BUILDER_IMAGE_NAME="operator-builder"
        echo ======================================================
        echo Building executable with Docker
        echo ======================================================
        rm -rf build/_output

        OPTS=""
        for i in "$@" ; do
          OPTS="$OPTS '$i'"
        done

        cat > "${BUILDER_IMAGE_NAME}.tmp" <<EODockerfile
FROM golang:1.13.7
WORKDIR /go/src/${OPERATOR_GO_PACKAGE}
ENV GO111MODULE=on
ENV GOPROXY=$go_proxy_url
COPY . .
RUN go generate ./pkg/...
RUN go test -test.short -mod=vendor ./cmd/... ./pkg/...
RUN GOOS=linux   GOARCH=amd64 go build $OPTS -o /dist/linux-amd64/syndesis-operator    -gcflags all=-trimpath=\${GOPATH} -asmflags all=-trimpath=\${GOPATH} -mod=vendor github.com/syndesisio/syndesis/install/operator/cmd/manager
RUN GOOS=darwin  GOARCH=amd64 go build $OPTS -o /dist/darwin-amd64/syndesis-operator   -gcflags all=-trimpath=\${GOPATH} -asmflags all=-trimpath=\${GOPATH} -mod=vendor github.com/syndesisio/syndesis/install/operator/cmd/manager
RUN GOOS=windows GOARCH=amd64 go build $OPTS -o /dist/windows-amd64/syndesis-operator  -gcflags all=-trimpath=\${GOPATH} -asmflags all=-trimpath=\${GOPATH} -mod=vendor github.com/syndesisio/syndesis/install/operator/cmd/manager
RUN GOOS=linux   GOARCH=amd64 go build $OPTS -o /dist/linux-amd64/platform-detect      -gcflags all=-trimpath=\${GOPATH} -asmflags all=-trimpath=\${GOPATH} -mod=vendor github.com/syndesisio/syndesis/install/operator/cmd/detect
RUN GOOS=darwin  GOARCH=amd64 go build $OPTS -o /dist/darwin-amd64/platform-detect     -gcflags all=-trimpath=\${GOPATH} -asmflags all=-trimpath=\${GOPATH} -mod=vendor github.com/syndesisio/syndesis/install/operator/cmd/detect
RUN GOOS=windows GOARCH=amd64 go build $OPTS -o /dist/windows-amd64/platform-detect    -gcflags all=-trimpath=\${GOPATH} -asmflags all=-trimpath=\${GOPATH} -mod=vendor github.com/syndesisio/syndesis/install/operator/cmd/detect
EODockerfile

        docker build -t "${BUILDER_IMAGE_NAME}" . -f "${BUILDER_IMAGE_NAME}.tmp"
        rm -f "${BUILDER_IMAGE_NAME}.tmp"

        echo ======================================================

        for GOARCH in amd64 ; do
          for GOOS in linux darwin windows ; do
            echo extracting operator executable to ./dist/${GOOS}-${GOARCH}/syndesis-operator
            mkdir -p ./dist/${GOOS}-${GOARCH}
            docker run "${BUILDER_IMAGE_NAME}" cat /dist/${GOOS}-${GOARCH}/syndesis-operator > ./dist/${GOOS}-${GOARCH}/syndesis-operator

            echo extracting platform-detect executable to ./dist/${GOOS}-${GOARCH}/platform-detect
            docker run "${BUILDER_IMAGE_NAME}" cat /dist/${GOOS}-${GOARCH}/platform-detect > ./dist/${GOOS}-${GOARCH}/platform-detect
          done
        done
        chmod a+x ./dist/*/*-*
        mkdir -p ./build/_output/bin
        cp ./dist/linux-amd64/syndesis-operator ./build/_output/bin/syndesis-operator
    ;;
    *)
        echo invalid build strategy: $strategy
        exit 1
    esac
}

build_image()
{
    local strategy="$1"
    local OPERATOR_IMAGE_NAME="$2"
    local OPERATOR_IMAGE_TAG="$3"
    local S2I_STREAM_NAME="$4"
    local REGISTRY="$5"

    local hasdocker=$(docker_is_available)
    local hasoc=$(is_oc_available)

    if [ "$strategy" == "auto" ] ; then

        if [ "$hasoc" == "OK" -o "$hasoc" == "$SETUP_MINISHIFT" ]; then
            strategy="s2i"
        elif [ "$hasdocker" == "OK" ]; then
            printf "\nWARN: Building image with 'docker' since 'oc' command is not available for s2i ... \n\t\t$hasoc\n"
            strategy="docker"
        else
            echo "ERROR: Building an image requires either 'docker' or 'oc' commands to be installed."
            exit 1
        fi
    fi

    case "$strategy" in
    "s2i")
        if [ "$hasoc" == "$SETUP_MINISHIFT" ]; then
            setup_minishift_oc > /dev/null
        elif [ "$hasoc" != "OK" ]; then
            check_error "$hasoc"
        fi

        #
        # Check that oc is logged in and communicating with a cluster
        #
        set +e
        version="$(oc version 2>&1)"
        set -e
        check_error "${version}"

        echo ======================================================
        echo Building image with S2I
        echo ======================================================

        #
        # If a build config already exists, check whether its pointing at the same
        # image name and tag as this one. If it does then can use it to rebuild.
        # Otherwise remove the build-config to repoint to the new image:tag.
        #
        local bcOutName="$(oc get bc "${S2I_STREAM_NAME}" -o=jsonpath='{.spec.output.to.name}' 2>&1)"
        check_error "${bcOutName}"

        local imgTag="${OPERATOR_IMAGE_NAME}:${OPERATOR_IMAGE_TAG}"
        # Remove any registry prefix from OPERATOR_IMAGE_NAME
        imgTag="${imgTag##*/}"

        if [ "${bcOutName}" != "${imgTag}" ]; then
            echo "Removing old BuildConfig ${S2I_STREAM_NAME} due to different image:tag"
            #
            # build config exists but not generated the same image & tag as is requested
            # so remove it to allow a new bc to be generated.
            #
            oc delete bc "${S2I_STREAM_NAME}" >/dev/null 2>&1
        fi

        if [ -z "$(oc get bc -o name | grep ${S2I_STREAM_NAME})" ]; then
            echo "Creating BuildConfig ${S2I_STREAM_NAME} with tag ${OPERATOR_IMAGE_TAG}"
            oc new-build --strategy=docker --binary=true --to=${OPERATOR_IMAGE_NAME}:${OPERATOR_IMAGE_TAG} --name ${S2I_STREAM_NAME}
        fi
        local arch="$(mktemp -t ${S2I_STREAM_NAME}-dockerXXX).tar"
        echo $arch
        trap "rm $arch" EXIT
        tar --exclude-from=.dockerignore -cvf $arch build
        if [ ! -d build ]; then
            echo "No build directory. Something failed with building image"
            exit 1
        fi

        cd build
        tar uvf $arch Dockerfile
        oc start-build --from-archive=$arch ${S2I_STREAM_NAME}
    ;;
    "docker")
        if [ "$hasdocker" != "OK" ]; then
            check_error "$hasdocker"
        fi

        FULL_OPERATOR_IMAGE_NAME=${OPERATOR_IMAGE_NAME}
        if [ -n "${REGISTRY}" ]; then
            #
            # Need to apply the registry to the image name so that the
            # operator is built with the correct image location
            #
            FULL_OPERATOR_IMAGE_NAME=${REGISTRY}/${OPERATOR_IMAGE_NAME}
        fi

        echo ======================================================
        echo Building image with Docker
        echo ======================================================
        docker build -f "build/Dockerfile" -t "${FULL_OPERATOR_IMAGE_NAME}:${OPERATOR_IMAGE_TAG}" .
        echo ======================================================
        echo "Operator Image Built: ${OPERATOR_IMAGE_NAME}"
        echo ======================================================

        if [ -n "${REGISTRY}" ]; then
            #
            # If registry defined then push image to docker registry
            #
            echo ======================================================
            echo Pushing image to docker registry
            echo ======================================================

            #
            # Checks the docker image has been built
            # and available to be pushed.
            #
            image_id=$(docker images --filter reference=${FULL_OPERATOR_IMAGE_NAME}:${OPERATOR_IMAGE_TAG} | grep -v IMAGE | awk '{print $3}' | uniq)
            if [ -z ${image_id} ]; then
                check_error "ERROR: Cannot find newly-built docker image of ${FULL_OPERATOR_IMAGE_NAME}:${OPERATOR_IMAGE_TAG}"
            fi

            #
            # Push to the registry
            #
            docker push ${FULL_OPERATOR_IMAGE_NAME}

            #
            # Check the image is present in the registry
            #
            status=$(curl -sLk https://${REGISTRY}/v2/${OPERATOR_IMAGE_NAME}/tags/list)
            if [ -z "${status##*errors*}" ] ;then
                check_error "ERROR: Cannot verify image has been pushed to registry."
            else
                echo ======================================================
                echo "Operator Image Pushed to Registry: ${REGISTRY}"
                echo ======================================================
            fi
        fi

    ;;
    *)
        echo invalid build strategy: $1
        exit 1
    esac
}

openapi_gen() {
    if hash openapi-gen 2>/dev/null; then
        openapi-gen --logtostderr=true -o "" \
            -i ./pkg/apis/syndesis/v1alpha1 -O zz_generated.openapi -p ./pkg/apis/syndesis/v1alpha1

        openapi-gen --logtostderr=true -o "" \
            -i ./pkg/apis/syndesis/v1beta1 -O zz_generated.openapi -p ./pkg/apis/syndesis/v1beta1
    else
        echo "skipping go openapi generation"
    fi
}
