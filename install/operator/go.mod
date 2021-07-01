module github.com/syndesisio/syndesis/install/operator

//
// Example of how you can use a local development branch for a module, just point to
// where it's checked out:
//
// replace github.com/chirino/hawtgo => /Users/chirino/sandbox/hawtgo

require (
	github.com/DATA-DOG/go-sqlmock v1.5.0
	github.com/aws/aws-sdk-go v1.38.20
	github.com/chirino/hawtgo v0.0.1
	github.com/docker/go-connections v0.4.0
	github.com/elazarl/goproxy v0.0.0-20190421051319-9d40249d3c2f // indirect
	github.com/elazarl/goproxy/ext v0.0.0-20190421051319-9d40249d3c2f // indirect
	github.com/go-logr/logr v0.4.0
	github.com/go-openapi/spec v0.20.3
	github.com/imdario/mergo v0.3.12
	github.com/lib/pq v1.9.0
	github.com/openshift/api v3.9.1-0.20190927182313-d4a64ec2cbd8+incompatible
	github.com/pkg/errors v0.9.1
	github.com/pmezard/go-difflib v1.0.0
	github.com/prometheus/client_golang v1.10.0
	github.com/robfig/cron/v3 v3.0.1
	github.com/shurcooL/httpfs v0.0.0-20190707220628-8d4bc4ba7749
	github.com/shurcooL/vfsgen v0.0.0-20181202132449-6a9ea43bcacd
	github.com/spf13/cast v1.3.1
	github.com/spf13/cobra v1.1.3
	github.com/spf13/pflag v1.0.5
	github.com/stretchr/testify v1.7.0
	github.com/testcontainers/testcontainers-go v0.3.1
	gopkg.in/yaml.v2 v2.4.0
	k8s.io/api v0.21.1
	k8s.io/apiextensions-apiserver v0.21.1
	k8s.io/apimachinery v0.21.1
	k8s.io/client-go v12.0.0+incompatible
	k8s.io/code-generator v0.21.0
	k8s.io/gengo v0.0.0-20210203185629-de9496dff47b
	k8s.io/kube-openapi v0.0.0-20210323165736-1a6458611d18
	k8s.io/utils v0.0.0-20210305010621-2afb4311ab10 // indirect
	sigs.k8s.io/controller-runtime v0.9.0
	sigs.k8s.io/yaml v1.2.0
)

replace (
	k8s.io/api => k8s.io/api v0.20.6
	k8s.io/apiextensions-apiserver => k8s.io/apiextensions-apiserver v0.21.0
	k8s.io/apimachinery => k8s.io/apimachinery v0.20.6
	k8s.io/client-go => k8s.io/client-go v0.20.6
	k8s.io/code-generator => k8s.io/code-generator v0.20.6
	sigs.k8s.io/controller-runtime => sigs.k8s.io/controller-runtime v0.7.0
)

require (
	github.com/blang/semver/v4 v4.0.0
	github.com/coreos/prometheus-operator v0.39.0 // indirect
	github.com/emicklei/go-restful v2.15.0+incompatible // indirect
	github.com/ghodss/yaml v1.0.1-0.20190212211648-25d852aebe32 // indirect
	github.com/go-logr/zapr v0.2.0
	github.com/go-openapi/swag v0.19.15 // indirect
	github.com/golang/protobuf v1.5.2 // indirect
	github.com/google/gofuzz v1.2.0 // indirect
	github.com/google/uuid v1.2.0 // indirect
	github.com/googleapis/gnostic v0.5.4 // indirect
	github.com/leanovate/gopter v0.2.9
	github.com/mailru/easyjson v0.7.7 // indirect
	github.com/operator-framework/api v0.8.0
	github.com/operator-framework/operator-lifecycle-manager v0.18.2
	github.com/operator-framework/operator-sdk v0.19.4
	github.com/prometheus/common v0.20.0 // indirect
	github.com/rogpeppe/go-internal v1.8.0
	github.com/spf13/afero v1.2.2
	go.uber.org/zap v1.16.0
	golang.org/x/mod v0.4.2 // indirect
	golang.org/x/oauth2 v0.0.0-20210323180902-22b0adad7558 // indirect
	google.golang.org/appengine v1.6.7 // indirect
)

replace github.com/operator-framework/operator-sdk => github.com/operator-framework/operator-sdk v0.19.4

replace golang.org/x/text => golang.org/x/text v0.3.3

replace (
	github.com/docker/docker => github.com/moby/moby v0.7.3-0.20190826074503-38ab9da00309 // Required by Helm
	github.com/openshift/api => github.com/openshift/api v0.0.0-20200205145930-e9d93e317dd1
)

go 1.14
