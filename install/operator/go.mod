module github.com/syndesisio/syndesis/install/operator

//
// Example of how you can use a local development branch for a module, just point to
// where it's checked out:
//
// replace github.com/chirino/hawtgo => /Users/chirino/sandbox/hawtgo

require (
	github.com/DATA-DOG/go-sqlmock v1.4.1
	github.com/aws/aws-sdk-go v1.34.0
	github.com/chirino/hawtgo v0.0.1
	github.com/docker/go-connections v0.4.0
	github.com/elazarl/goproxy v0.0.0-20190421051319-9d40249d3c2f // indirect
	github.com/elazarl/goproxy/ext v0.0.0-20190421051319-9d40249d3c2f // indirect
	github.com/go-logr/logr v0.1.0
	github.com/go-openapi/spec v0.19.8
	github.com/imdario/mergo v0.3.10
	github.com/lib/pq v1.8.0
	github.com/openshift/api v3.9.1-0.20190927182313-d4a64ec2cbd8+incompatible
	github.com/pkg/errors v0.9.1
	github.com/pmezard/go-difflib v1.0.0
	github.com/prometheus/client_golang v1.5.1
	github.com/robfig/cron/v3 v3.0.1
	github.com/shurcooL/httpfs v0.0.0-20190707220628-8d4bc4ba7749
	github.com/shurcooL/vfsgen v0.0.0-20181202132449-6a9ea43bcacd
	github.com/spf13/cast v1.3.1
	github.com/spf13/cobra v1.0.0
	github.com/spf13/pflag v1.0.5
	github.com/stretchr/testify v1.5.1
	github.com/testcontainers/testcontainers-go v0.5.1
	gopkg.in/yaml.v2 v2.3.0
	k8s.io/api v0.18.6
	k8s.io/apimachinery v0.18.6
	k8s.io/client-go v12.0.0+incompatible
	k8s.io/code-generator v0.18.4
	k8s.io/gengo v0.0.0-20200114144118-36b2048a9120
	k8s.io/kube-openapi v0.0.0-20200410145947-61e04a5be9a6
	sigs.k8s.io/yaml v1.2.0
)

require (
	github.com/RHsyseng/operator-utils v0.0.0-20200709142328-d5a5812a443f
	github.com/coreos/prometheus-operator v0.39.0 // indirect
	github.com/ghodss/yaml v1.0.1-0.20190212211648-25d852aebe32
	github.com/go-logr/zapr v0.1.1
	github.com/operator-framework/api v0.3.5
	github.com/operator-framework/operator-lifecycle-manager v0.0.0-20200508205316-9ffa1fdb8dcf // Tag v0.15.0
	github.com/operator-framework/operator-marketplace v0.0.0-20200515051804-e0148822df25
	github.com/operator-framework/operator-sdk v0.0.0-20200428193249-b34ae44ff198 // Not a release but a bump commit aligning with client-go 0.18.2
	github.com/spf13/afero v1.2.2
	go.uber.org/zap v1.15.0
	sigs.k8s.io/controller-runtime v0.6.1
)

replace k8s.io/client-go => k8s.io/client-go v0.18.2 // Required by prometheus-operator

replace (
	github.com/docker/docker => github.com/moby/moby v0.7.3-0.20190826074503-38ab9da00309 // Required by Helm
	github.com/openshift/api => github.com/openshift/api v0.0.0-20200205145930-e9d93e317dd1
)

go 1.14
