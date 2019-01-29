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
	Prometheus       string
	Grafana          string
	PostgresExporter string
}

type syndesisImages struct {
	Rest     string
	Ui       string
	Verifier string
	S2i      string
	Upgrade  string
}

type images struct {
	Support                     supportImages
	Syndesis                    syndesisImages
	ImageStreamNamespace        string
	SyndesisImagesPrefix        string
	OAuthProxyImagePrefix       string
	PrometheusImagePrefix       string
	GrafanaImagePrefix          string
	PostgresExporterImagePrefix string
}

type tags struct {
	Syndesis         string
	Postgresql       string
	OAuthProxy       string
	Prometheus       string
	Upgrade          string
	Grafana          string
	PostgresExporter string
}

type Dashboard struct {
	FileName string
	Json     string
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
	WithOAuthClient  bool
	Dashboards       []Dashboard
}

// TODO: Could be added from a local configuration file
var syndesisContext = Context{
	Images: images{
		SyndesisImagesPrefix:        "syndesis",
		OAuthProxyImagePrefix:       "openshift",
		PrometheusImagePrefix:       "prom",
		GrafanaImagePrefix:          "grafana",
		PostgresExporterImagePrefix: "wrouesnel",
		Support: supportImages{
			Postgresql:       "postgresql",
			OAuthProxy:       "oauth-proxy",
			Prometheus:       "prometheus",
			Grafana:          "grafana",
			PostgresExporter: "postgres_exporter",
		},
		Syndesis: syndesisImages{
			Rest:     "syndesis-server",
			Ui:       "syndesis-ui",
			Verifier: "syndesis-meta",
			S2i:      "syndesis-s2i",
			Upgrade:  "syndesis-upgrade",
		},
	},
	Tags: tags{
		Postgresql:       "9.5",
		OAuthProxy:       "v1.1.0",
		Prometheus:       "v2.1.0",
		Grafana:          "5.4.2",
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
		GrafanaImagePrefix:          "grafana",
		PostgresExporterImagePrefix: "wrouesnel",
		Support: supportImages{
			Postgresql:       "postgresql",
			OAuthProxy:       "oauth-proxy",
			Prometheus:       "prometheus",
			Grafana:          "grafana",
			PostgresExporter: "postgres_exporter",
		},
		Syndesis: syndesisImages{
			Rest:     "fuse-ignite-server",
			Ui:       "fuse-ignite-ui",
			Verifier: "fuse-ignite-meta",
			S2i:      "fuse-ignite-s2i",
			Upgrade:  "fuse-ignite-upgrade",
		},
	},
	Tags: tags{
		Postgresql:       "9.5",
		OAuthProxy:       "v1.1.0",
		Prometheus:       "v2.1.0",
		Grafana:          "5.4.2",
		PostgresExporter: "v0.4.7",
	},
	Registry: "registry.fuse-ignite.openshift.com",
}

var context = syndesisContext

func init() {
	flags := installCommand.PersistentFlags()

	flags.StringVar(&context.Name, "name", "syndesis", "Name of the template")
	flags.BoolVar(&context.AllowLocalHost, "allow-localhost", false, "Allow localhost")
	flags.BoolVar(&context.WithDockerImages, "with-docker-images", false, "With docker images")
	flags.StringVar(&context.Tags.Syndesis, "syndesis-tag", "latest", "Syndesis Image tag to use")
	flags.StringVar(&context.Tags.Upgrade, "upgrade-tag", "latest", "Syndesis Upgrade version")
	flags.BoolVar(&context.Oso, "oso", false, "Generate product templates for SO")
	flags.BoolVar(&context.Ocp, "ocp", false, "Generate product templates for OCP")
	flags.BoolVar(&context.EarlyAccess, "early-access", false, "Point repositories to early-access repos")
	flags.StringVar(&context.Registry, "registry", "docker.io", "Registry to use for imagestreams")
	flags.BoolVar(&context.Debug, "debug", false, "Enable debug support")
	flags.BoolVar(&context.WithOAuthClient, "with-oauth-client", false, "With OAuthClient")
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

	regex := regexp.MustCompile("(?m)^(.*)$")
	dashboardDir := "../dashboards/"
	dashboardFiles, err := ioutil.ReadDir(dashboardDir)
	check(err)

	for _, dashboardFile := range dashboardFiles {
		if strings.HasSuffix(dashboardFile.Name(), ".json") {
			json, err := ioutil.ReadFile(dashboardDir + dashboardFile.Name())
			check(err)
			dashboard := Dashboard{
				FileName: dashboardFile.Name(),
				Json:     regex.ReplaceAllString(string(json), "      $1"),
			}
			context.Dashboards = append(context.Dashboards, dashboard)
		}
	}

	files, err := ioutil.ReadDir("./")
	check(err)

	sort.Slice(files, func(i, j int) bool {
		return files[i].Name() < files[j].Name()
	})

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
