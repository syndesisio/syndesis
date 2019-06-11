#
# Some reusable shell functions:
#

# Outputs the value of a command line option.
# Usage: readopt <flag> <default value>
# Example: kind=$(readopt --kind MyKind)
readopt() {
    local filter="$1"
    local default="$2"
    local next=false
    for var in "${ARGS[@]}"; do
        if $next; then
            local value="${var##-}"
            if [ "$value" != "$var" ]; then
               # Next is already also option, so we haven't
               # specified a value.
               return
            fi
            echo $var
            break;
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
    echo $default
}

build_operator()
{
    local strategy="$1"
    if [ "$strategy" == "auto" ] ; then

        if [[ "$(which docker)" == "" ]] ; then
            if [[ "$(which go)" == "" ]] ; then
                echo 'error: you must have either docker or go installed to build this project.'
                exit 1
            elif [[ "$(pwd)" != "$GOPATH/src/${OPERATOR_GO_PACKAGE}" ]] ; then
                echo 'error: cannot do local go build: project not checked out into the $GOPATH directory.'
                echo '       either: 1) install docker'
                echo "           or: 2) check out project into: $GOPATH"
                echo ''
                exit 1
            else
                strategy="go"
            fi
        else
            if [[ "$(which go)" == "" ]] ; then
                echo 'warn: building with docker since you do not have go installed.'
                strategy="docker"
            elif [[ "$(pwd)" != "$GOPATH/src/${OPERATOR_GO_PACKAGE}" ]] ; then
                echo 'warn: cannot do local go build: project not checked out into the $GOPATH directory.'
                echo '      building with docker instead...'
                echo ''
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
        go generate ./pkg/...
        if [[ "$(which operator-sdk)" == "" ]] ; then
            operator-sdk generate k8s
            operator-sdk generate openapi
        fi

        go test ./cmd/... ./pkg/...
        GOOS=linux GOARCH=amd64 \
        go build -o ./build/_output/bin/operator \
            -gcflags all=-trimpath=${GOPATH} -asmflags all=-trimpath=${GOPATH} -mod=vendor \
            ${OPERATOR_GO_PACKAGE}/cmd/manager
    ;;
    "docker")
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
        if [ -z "$(oc get bc -o name | grep ${S2I_IMAGE_NAME})" ]; then
            echo "Creating BuildConfig ${S2I_IMAGE_NAME}"
            oc new-build --strategy=docker --binary=true --name ${S2I_IMAGE_NAME}
        fi
        local arch="$(mktemp -t ${S2I_IMAGE_NAME}-docker).tar"
        echo $arch
        trap "rm $arch" EXIT
        tar cvf $arch build
        cd build
        tar uvf $arch Dockerfile
        oc start-build --from-archive=$arch ${S2I_IMAGE_NAME}
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

