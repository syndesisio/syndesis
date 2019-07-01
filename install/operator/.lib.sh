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


docker_is_available() {
    if [[ "$(which docker)" == "" ]] ; then
        echo "warn: docker command not installed"
        echo
        return 0
    fi

    if docker info > /dev/null 2> /dev/null ; then
        return 1
    else
        echo "warn: the docker comamand is not conected to a server"
        echo
        return 0
    fi
}

build_operator()
{
    local strategy="$1"
    local gopackage="$2"
    if [ "$strategy" == "auto" ] ; then

        if docker_is_available; then
            if [[ "$(which go)" == "" ]] ; then
                echo 'error: you must have either docker or go installed to build this project.'
                exit 1
            else
                strategy="go"
            fi
        else
            if [[ "$(which go)" == "" ]] ; then
                echo 'warn: building with docker since you do not have go installed.'
                strategy="docker"
            else
                strategy="go"
            fi
        fi
    fi


    case "$strategy" in
    "go")
        echo ======================================================
        echo Building executable with go tooling
        echo ======================================================
        export GO111MODULE=on

        go mod vendor
        if [[ "$(which operator-sdk)" != "" ]] ; then
            if [[ "$(pwd)" != "$GOPATH/src/${gopackage}" ]] ; then
                echo
                echo "warnning: operator-sdk only works on project's in the \$GOPATH"
                echo "          can't use it to update the generated code"
                echo "          please move this project under the \$GOPATH so that :'$(pwd)'"
                echo "          is located at '$GOPATH/src/${gopackage}'"
                echo
            else
                operator-sdk generate k8s
                # operator-sdk generate openapi
            fi
        fi
        go generate ./pkg/...

        echo building executable
        go test ./cmd/... ./pkg/...
        echo building executable
        GOOS=linux GOARCH=amd64 go build -o ./build/_output/bin/operator \
            -gcflags all=-trimpath=${GOPATH} -asmflags all=-trimpath=${GOPATH} -mod=vendor \
            ./cmd/manager
    ;;
    "docker")

        local BUILDER_IMAGE_NAME="operator-builder"
        echo ======================================================
        echo Building executable with Docker
        echo ======================================================
        rm -rf build/_output
        docker build -t "${BUILDER_IMAGE_NAME}" .

        echo ======================================================
        echo Extracting executable from Docker
        echo ======================================================
        mkdir -p ./build/_output/bin
        docker run "${BUILDER_IMAGE_NAME}" cat /operator > ./build/_output/bin/operator
        chmod a+x ./build/_output/bin/operator
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
    local S2I_STREAM_NAME="$3"

    if [ "$strategy" == "auto" ] ; then
        strategy=docker
        if [ -n "$(which oc)" ] ; then
            strategy=s2i
        fi
    fi

    case "$strategy" in
    "s2i")
        echo ======================================================
        echo Building image with S2I
        echo ======================================================
        if [ -z "$(oc get bc -o name | grep ${S2I_STREAM_NAME})" ]; then
            echo "Creating BuildConfig ${S2I_STREAM_NAME}"
            oc new-build --strategy=docker --binary=true --name ${S2I_STREAM_NAME}
        fi
        local arch="$(mktemp -t ${S2I_STREAM_NAME}-dockerXXX).tar"
        echo $arch
        trap "rm $arch" EXIT
        tar cvf $arch build
        cd build
        tar uvf $arch Dockerfile
        oc start-build --from-archive=$arch ${S2I_STREAM_NAME}
    ;;
    "docker")
        echo ======================================================
        echo Building image with Docker
        echo ======================================================
        docker build -f "build/Dockerfile" -t "${OPERATOR_IMAGE_NAME}" .
        echo ======================================================
        echo "Operator Image Built: ${OPERATOR_IMAGE_NAME}"
        echo ======================================================
    ;;
    *)
        echo invalid build strategy: $1
        exit 1
    esac
}
