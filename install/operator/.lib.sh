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

build_container_operator()
{
    local container_cmd="${1:-}"
    shift
    local goos="$1"
    shift
    local goarch="$1"
    shift
    local go_proxy_url="$1"
    shift

    if [ -z "${container_cmd}" ]; then
        echo "ERROR: Container command is not defined. Either podman or docker are supported."
        exit 1
    fi

    local BUILDER_IMAGE_NAME="operator-builder"

  ${container_cmd} build --target=builder \
      -e GOOS=${goos} -e GOARCH=${goarch} -e GOPROXY=${go_proxy_url} \
      --build-arg CONTAINER_REGISTRY=${CONTAINER_REGISTRY} \
      --build-arg IMAGE_NAMESPACE=${IMAGE_NAMESPACE} \
      --build-arg IMAGE_NAME=${IMAGE_NAME} \
      --build-arg IMAGE_TAG=${IMAGE_TAG} \
      --build-arg BUILD_TIME=${BUILD_TIME} \
      --build-arg GO_BUILD_OPTIONS=${GO_BUILD_OPTIONS} \
      -t "${BUILDER_IMAGE_NAME}" . -f ./build/Dockerfile

  echo ======================================================

  echo extracting operator executable to ./dist/${goos}-${goarch}/syndesis-operator
  mkdir -p ./dist/${goos}-${goarch}
  ${container_cmd} run "${BUILDER_IMAGE_NAME}" cat /out/syndesis-operator > ./dist/${goos}-${goarch}/syndesis-operator

  echo extracting platform-detect executable to ./dist/${goos}-${goarch}/platform-detect
  ${container_cmd} run "${BUILDER_IMAGE_NAME}" cat /out/platform-detect > ./dist/${goos}-${goarch}/platform-detect
  chmod a+x ./dist/*/*-*
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
    local haspodman=$(podman_is_available)
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

        LDFLAGS="-X github.com/syndesisio/syndesis/install/operator/pkg.DefaultOperatorImage=${CONTAINER_REGISTRY}/${IMAGE_NAMESPACE}/${IMAGE_NAME} -X github.com/syndesisio/syndesis/install/operator/pkg.DefaultOperatorTag=${IMAGE_TAG} -X github.com/syndesisio/syndesis/install/operator/pkg.BuildDateTime=${BUILD_TIME}"
        for GOARCH in amd64 ; do
          for GOOS in ${GOOSLIST} ; do
            export GOARCH GOOS
            echo building ./dist/${GOOS}-${GOARCH}/syndesis-operator executable
            go build -ldflags "${LDFLAGS}" ${GO_BUILD_OPTIONS} \
            -o ./dist/${GOOS}-${GOARCH}/syndesis-operator \
                -gcflags all=-trimpath=${GOPATH} -asmflags all=-trimpath=${GOPATH} -mod=vendor \
                ./cmd/manager

            echo building ./dist/${GOOS}-${GOARCH}/platform-detect executable
            go build -ldflags "${LDFLAGS}" ${GO_BUILD_OPTIONS} \
            -o ./dist/${GOOS}-${GOARCH}/platform-detect \
                -gcflags all=-trimpath=${GOPATH} -asmflags all=-trimpath=${GOPATH} -mod=vendor \
                ./cmd/detect
          done
        done
        mkdir -p ./build/_output/bin
        cp ./dist/linux-amd64/syndesis-operator ./build/_output/bin/syndesis-operator

    ;;
    "podman")
        if [ "$hasdocker" != "OK" ]; then
            echo "$hasdocker"
            exit 1
        fi
        echo ======================================================
        echo Building executable with podman
        echo ======================================================

        rm -rf build/_output
        for GOARCH in amd64 ; do
          for GOOS in ${GOOSLIST} ; do
            build_container_operator "podman" "$GOARCH" "$GOOS" "$go_proxy_url"
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
        echo ======================================================
        echo Building executable with docker
        echo ======================================================

        rm -rf build/_output
        for GOARCH in amd64 ; do
          for GOOS in ${GOOSLIST} ; do
            build_container_operator "docker" "$GOARCH" "$GOOS" "$go_proxy_url"
          done
        done
        mkdir -p ./build/_output/bin
        cp ./dist/linux-amd64/syndesis-operator ./build/_output/bin/syndesis-operator
    ;;
    *)
        echo invalid build strategy: $strategy
        exit 1
    esac
}

#
# container_command
# registry
# image_namespace
# image_name
# image_tag
#
build_container_image()
{
    local container_cmd="${1:-}"

    if [ -z "${container_cmd}" ]; then
        echo "ERROR: Container command is not defined. Either podman or docker are supported."
        exit 1
    fi


    if [ -n "${IMAGE_NAMESPACE}" ]; then
      full_image_name="${IMAGE_NAMESPACE}/${IMAGE_NAME}"
    else
      full_image_name="${IMAGE_NAME}"
    fi

    if [ -n "${CONTAINER_REGISTRY}" ]; then
        #
        # Need to apply the registry to the image name so that the
        # operator image is built with the correct image location
        #
        full_image_name=${CONTAINER_REGISTRY}/${full_image_name}
    fi

    echo ======================================================
    echo Building image with ${container_cmd}
    echo ======================================================
    ${container_cmd} build -f "build/Dockerfile" -t "${full_image_name}:${IMAGE_TAG}" .
    echo ======================================================
    echo "Operator Image Built: ${full_image_name}"
    echo ======================================================

    if [ "${CONTAINER_REGISTRY}" == "docker.io" ] && [ "${IMAGE_NAMESPACE}" == "syndesis" ]; then
        #
        # Do not push if registry and namespace are the defaults
        #
        return
    elif [ -n "${CONTAINER_REGISTRY}" ]; then
        #
        # If registry defined then push image to container registry
        #
        echo ======================================================
        echo Pushing image to container registry: ${CONTAINER_REGISTRY}
        echo ======================================================

        #
        # Checks the container image has been built
        # and available to be pushed.
        #
        image_id=$(${container_cmd} images --filter reference=${full_image_name}:${IMAGE_TAG} | grep -v IMAGE | awk '{print $3}' | uniq)
        if [ -z ${image_id} ]; then
            check_error "ERROR: Cannot find newly-built container image of ${full_image_name}:${IMAGE_TAG}"
        fi


        #
        # Push to the registry
        #
        if [ "${container_cmd}" == "docker" ]; then
            ${container_cmd} push "${full_image_name}:${IMAGE_TAG}"
        elif [ "${container_cmd}" == "podman" ]; then
            ${container_cmd} push "${image_id}" "${full_image_name}:${IMAGE_TAG}"
        else
            echo "Pushing to registry not supported by ${container_cmd}"
        fi

        #
        # Check the image is present in the registry
        #
        status=$(curl -sLk https://${CONTAINER_REGISTRY}/v2/${IMAGE_NAMESPACE}/${IMAGE_NAME}/tags/list)
        if [ -z "${status##*errors*}" ] ;then
            check_error "ERROR: Cannot verify image has been pushed to registry."
        else
            echo ======================================================
            echo "Operator Image Pushed to Registry: ${CONTAINER_REGISTRY}"
            echo ======================================================
        fi
    fi
}

#
# Parameters:
# IMAGE_BUILD_MODE   - [auto, s2i, docker, podman]
# s2i_stream_name    - syndesis-operator by default
#
build_image()
{
    local strategy="${1:-auto}"
    local s2i_stream_name="${2:-syndesis-operator}"

    local hasdocker=$(docker_is_available)
    local haspodman=$(podman_is_available)
    local hasoc=$(is_oc_available)

    if [ "$strategy" == "auto" ] ; then

        if [ "$hasoc" == "OK" -o "$hasoc" == "$SETUP_MINISHIFT" ]; then
            strategy="s2i"
        elif [ "$haspodman" == "OK" ]; then
            printf "\nWARN: 'oc' not found but 'podman' found - building image ... \n"
            strategy="podman"
        elif [ "$hasdocker" == "OK" ]; then
            printf "\nWARN: 'oc' not found but 'docker' found - building image ... \n"
            strategy="docker"
        else
            echo "ERROR: Building an image requires either 'oc', 'podman' or 'docker' to be installed."
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
        local bcOutName="$(oc get bc "${s2i_stream_name}" -o=jsonpath='{.spec.output.to.name}' 2>&1)"
        if [ "$(contains_error "${bcOutName}")" != "YES" ]; then

            local imgTag="${image_name}:${image_tag}"
            if [ "${bcOutName}" != "${imgTag}" ]; then
                echo "Removing old BuildConfig ${s2i_stream_name} due to different image:tag"
                #
                # build config exists but not generated the same image & tag as is requested
                # so remove it to allow a new bc to be generated.
                #
                oc delete bc "${s2i_stream_name}" >/dev/null 2>&1
            fi
        fi

        if [ -z "$(oc get bc -o name | grep ${s2i_stream_name})" ]; then
            echo "Creating BuildConfig ${s2i_stream_name} with tag ${IMAGE_TAG}"
            oc new-build --strategy=docker --binary=true --to="${IMAGE_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}" --name ${s2i_stream_name}
        fi
        local arch="$(mktemp -t ${s2i_stream_name}-dockerXXX).tar"
        echo $arch
        trap "rm $arch" EXIT
        tar --exclude-from=.dockerignore -cvf $arch build
        if [ ! -d build ]; then
            echo "No build directory. Something failed with building image"
            exit 1
        fi

        cd build
        tar uvf $arch Dockerfile
        oc start-build --from-archive=$arch ${s2i_stream_name}
    ;;
    "docker")
        if [ "$hasdocker" != "OK" ]; then
            check_error "$hasdocker"
        fi

        build_container_image "docker"
    ;;
    "podman")
        if [ "$haspodman" != "OK" ]; then
            check_error "$hasdocker"
        fi

        build_container_image "podman"
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
