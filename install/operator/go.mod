module github.com/syndesisio/syndesis/install/operator

//
// Example of how you can use a local development branch for a module, just point to
// where it's checked out:
//
// replace github.com/chirino/hawtgo => /Users/chirino/sandbox/hawtgo

require (
	github.com/DATA-DOG/go-sqlmock v1.4.1
	github.com/aws/aws-sdk-go v1.30.9
	github.com/chirino/hawtgo v0.0.1
	github.com/docker/go-connections v0.4.0
	github.com/elazarl/goproxy v0.0.0-20190421051319-9d40249d3c2f // indirect
	github.com/elazarl/goproxy/ext v0.0.0-20190421051319-9d40249d3c2f // indirect
	github.com/go-logr/logr v0.2.0
	github.com/go-openapi/spec v0.19.8
	github.com/imdario/mergo v0.3.9
	github.com/lib/pq v1.3.0
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
	github.com/stretchr/testify v1.6.1
	github.com/testcontainers/testcontainers-go v0.3.1
	gopkg.in/yaml.v2 v2.2.8
	k8s.io/api v0.20.4
	k8s.io/apimachinery v0.20.4
	k8s.io/client-go v12.0.0+incompatible
	k8s.io/code-generator v0.18.2
	k8s.io/gengo v0.0.0-20200413195148-3a45101e95ac
	k8s.io/kube-openapi v0.0.0-20201113171705-d219536bb9fd
	sigs.k8s.io/yaml v1.2.0
)

require (
	github.com/blang/semver v3.5.1+incompatible
	github.com/coreos/prometheus-operator v0.39.0 // indirect
	github.com/ghodss/yaml v1.0.1-0.20190212211648-25d852aebe32 // indirect
	github.com/go-logr/zapr v0.1.1
	github.com/go-openapi/swag v0.19.7 // indirect
	github.com/operator-framework/api v0.3.5
	github.com/operator-framework/operator-lifecycle-manager v0.0.0-20200521062108-408ca95d458f // Tag 0.15.1
	github.com/operator-framework/operator-marketplace v0.0.0-20200515051804-e0148822df25
	github.com/operator-framework/operator-sdk v0.0.0-20200428193249-b34ae44ff198 // Not a release but a bump commit aligning with client-go 0.18.2
	github.com/rogpeppe/go-internal v1.5.0
	github.com/spf13/afero v1.2.2
	go.uber.org/zap v1.14.1
	golang.org/x/lint v0.0.0-20200130185559-910be7a94367 // indirect
	golang.org/x/oauth2 v0.0.0-20200107190931-bf48bf16ab8d // indirect
	sigs.k8s.io/controller-runtime v0.6.0
)

replace k8s.io/client-go => k8s.io/client-go v0.18.2 // Required by prometheus-operator

replace (
	github.com/docker/docker => github.com/moby/moby v0.7.3-0.20190826074503-38ab9da00309 // Required by Helm
	github.com/openshift/api => github.com/openshift/api v0.0.0-20200205145930-e9d93e317dd1
)

go 1.14
