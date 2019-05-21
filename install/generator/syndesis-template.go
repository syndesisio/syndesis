// To install, run:
//
//   go get -u github.com/spf13/cobra github.com/spf13/pflag github.com/hoisie/mustache
//   go install syndesis-template.go
//
// Or to just run without installing:
//
//   go run syndesis-template.go <args>
//
package main

import (
	//"flag"
	"flag"
	"fmt"
	"io/ioutil"
	"log"
	"regexp"
	"sort"
	"strings"

	"github.com/hoisie/mustache"
	"github.com/imdario/mergo"
	"github.com/spf13/cobra"
	"github.com/spf13/pflag"
)

func main() {
	err := installCommand.Execute()
	check(err)
}

var installCommand = &cobra.Command{
	Use:   "syndesis-template",
	Short: "syndesis-template is a tool for creating the OpenShift templates used to install Syndesis.",
	Long:  `syndesis-template is a tool for creating the OpenShift templates used to install Syndesis.`,
	Run:   install,
}

type supportImages struct {
	Postgresql       string
	OAuthProxy       string
	OAuthProxyImage  string
	Prometheus       string
	PostgresExporter string
}

type syndesisImages struct {
	Rest     string
	Ui       string
	Verifier string
	S2i      string
	Upgrade  string
	Komodo   string
}

type images struct {
	Support                     supportImages
	Syndesis                    syndesisImages
	ImageStreamNamespace        string
	SyndesisImagesPrefix        string
	OAuthProxyImagePrefix       string
	PrometheusImagePrefix       string
	PostgresExporterImagePrefix string
	KomodoImagesPrefix          string
}

type tags struct {
	Syndesis         string
	Postgresql       string
	OAuthProxy       string
	Prometheus       string
	Upgrade          string
	PostgresExporter string
	Komodo           string
}

type Context struct {
	Name             string
	AllowLocalHost   bool
	WithDockerImages bool
	Productized      bool
	EarlyAccess      bool
	Oso              bool
	Ocp              bool
	Tag              string
	Registry         string
	Images           images
	Tags             tags
	Debug            bool
	PrometheusRules  string
	IncludeDint      bool
}

// TODO: Could be added from a local configuration file
var syndesisContext = Context{
	Images: images{
		SyndesisImagesPrefix:        "syndesis",
		OAuthProxyImagePrefix:       "quay.io/openshift",
		PrometheusImagePrefix:       "prom",
		PostgresExporterImagePrefix: "wrouesnel",
		KomodoImagesPrefix:          "teiid",
		Support: supportImages{
			Postgresql:       "postgresql",
			OAuthProxy:       "oauth-proxy",
			OAuthProxyImage:  "origin-oauth-proxy",
			Prometheus:       "prometheus",
			PostgresExporter: "postgres_exporter",
		},
		Syndesis: syndesisImages{
			Rest:     "syndesis-server",
			Ui:       "syndesis-ui",
			Verifier: "syndesis-meta",
			S2i:      "syndesis-s2i",
			Upgrade:  "syndesis-upgrade",
			Komodo:   "komodo-server",
		},
	},
	Tags: tags{
		Postgresql:       "9.5",
		OAuthProxy:       "v4.0.0",
		Prometheus:       "v2.1.0",
		PostgresExporter: "v0.4.7",
	},
}

// TODO: Update with product image references here
var productContext = Context{
	Images: images{
		ImageStreamNamespace:        "fuse-ignite",
		SyndesisImagesPrefix:        "fuse7",
		OAuthProxyImagePrefix:       "openshift",
		PrometheusImagePrefix:       "prom",
		PostgresExporterImagePrefix: "wrouesnel",
		KomodoImagesPrefix:          "dv",
		Support: supportImages{
			Postgresql:       "postgresql",
			OAuthProxy:       "oauth-proxy",
			Prometheus:       "prometheus",
			PostgresExporter: "postgres_exporter",
		},
		Syndesis: syndesisImages{
			Rest:     "fuse-ignite-server",
			Ui:       "fuse-ignite-ui",
			Verifier: "fuse-ignite-meta",
			S2i:      "fuse-ignite-s2i",
			Upgrade:  "fuse-ignite-upgrade",
			Komodo:   "fuse-komodo-server",
		},
	},
	Tags: tags{
		Postgresql:       "9.5",
		OAuthProxy:       "v1.1.0",
		Prometheus:       "v2.1.0",
		PostgresExporter: "v0.4.7",
	},
	Registry: "registry.fuse-ignite.openshift.com",
}

var context = syndesisContext
var prometheusRulesFile = ""

var prometheusRulesIdentEx = regexp.MustCompile(`(.+)`)

func init() {
	flags := installCommand.PersistentFlags()

	flags.StringVar(&context.Name, "name", "syndesis", "Name of the template")
	flags.BoolVar(&context.AllowLocalHost, "allow-localhost", false, "Allow localhost")
	flags.BoolVar(&context.WithDockerImages, "with-docker-images", false, "With docker images")
	flags.StringVar(&context.Tags.Syndesis, "syndesis-tag", "latest", "Syndesis Image tag to use")
	flags.StringVar(&context.Tags.Upgrade, "upgrade-tag", "latest", "Syndesis Upgrade version")
	flags.StringVar(&context.Tags.Komodo, "komodo-tag", "latest", "Komodo image tag to use")
	flags.BoolVar(&context.Oso, "oso", false, "Generate product templates for SO")
	flags.BoolVar(&context.Ocp, "ocp", false, "Generate product templates for OCP")
	flags.BoolVar(&context.EarlyAccess, "early-access", false, "Point repositories to early-access repos")
	flags.StringVar(&context.Registry, "registry", "docker.io", "Registry to use for imagestreams")
	flags.BoolVar(&context.Debug, "debug", false, "Enable debug support")
	flags.StringVar(&prometheusRulesFile, "prometheus-rules-file", "", "Prometheus Rules file to copy as content of a configmap")
	flags.BoolVar(&context.IncludeDint, "include-data-integration", true, "Include Data Integration")
	pflag.CommandLine.AddGoFlagSet(flag.CommandLine)
}

func install(cmd *cobra.Command, args []string) {

	if context.Oso || context.Ocp {
		context.Productized = true
		if err := mergo.MergeWithOverwrite(&context, productContext); err != nil {
			log.Fatal("Cannot merge in product image names")
		}
		if context.Oso {
			context.Name = context.Name + "-" + context.Tags.Syndesis
		}
	}

	files, err := ioutil.ReadDir("./")
	check(err)

	sort.Slice(files, func(i, j int) bool {
		return files[i].Name() < files[j].Name()
	})

	prometheusRules, err := ioutil.ReadFile(prometheusRulesFile)
	check(err)

	context.PrometheusRules = prometheusRulesIdentEx.ReplaceAllString(string(prometheusRules), "      $1")

	for _, f := range files {
		if strings.HasSuffix(f.Name(), ".yml.mustache") {
			template, err := ioutil.ReadFile(f.Name())
			check(err)
			fmt.Print(mustache.Render(string(template), context))
		}
	}

}

func check(e error) {
	if e != nil {
		panic(e)
	}
}
