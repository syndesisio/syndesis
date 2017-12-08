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
	Postgresql    string
	OAuthProxy    string
}

type syndesisImages struct {
	Rest     string
	Ui       string
	Verifier string
	Atlasmap string
}

type s2iConfig struct {
	ImageStream string
	DockerImage string
	Tag         string
}

type images struct {
	Support               supportImages
	Syndesis              syndesisImages
	S2i                   s2iConfig
	ImageStreamNamespace  string
	SyndesisImagesPrefix  string
	AtlasMapImagesPrefix  string
	OAuthProxyImagePrefix string
}

type tags struct {
	Syndesis      string
	Atlasmap      string
	Postgresql    string
	OAuthProxy    string
}

type Context struct {
	Name                          string
	AllowLocalHost                bool
	WithDockerImages              bool
	Ephemeral                     bool
	Restricted                    bool
	Probeless                     bool
	Productized                   bool
	Tag                           string
	Registry                      string
	Images                        images
	Tags                          tags
	Debug						  bool
}

// TODO: Could be added from a local configuration file
var syndesisContext = Context{
	Images: images{
		SyndesisImagesPrefix:  "syndesis",
		AtlasMapImagesPrefix:  "atlasmap",
		OAuthProxyImagePrefix: "openshift",
		Support: supportImages{
			Postgresql:    "postgresql",
			OAuthProxy:    "oauth-proxy",
		},
		Syndesis: syndesisImages{
			Rest:     "syndesis-rest",
			Ui:       "syndesis-ui",
			Verifier: "syndesis-verifier",
			Atlasmap: "atlasmap",
		},
		S2i: s2iConfig{
			ImageStream: "syndesis-s2i",
			DockerImage: "syndesis/syndesis-s2i",
			Tag:         "latest",
		},
	},
	Tags: tags{
		Postgresql:    "9.5",
		OAuthProxy:    "v1.1.0",
	},
}

// TODO: Update with product image references here
var productContext = Context{
	Images: images{
		ImageStreamNamespace:  "fuse-ignite",
		SyndesisImagesPrefix:  "syndesis",
		AtlasMapImagesPrefix:  "atlasmap",
		OAuthProxyImagePrefix: "openshift",
		Support: supportImages{
			Postgresql:    "postgresql",
			OAuthProxy:    "oauth-proxy",
		},
		Syndesis: syndesisImages{
			Rest:     "fuse-ignite-rest",
			Ui:       "fuse-ignite-ui",
			Verifier: "fuse-ignite-verifier",
			Atlasmap: "fuse-ignite-mapper",
		},
		S2i: s2iConfig{
			ImageStream: "fuse-ignite-java-openshift",
			DockerImage: "jboss-fuse7-tech-preview/fuse-ignite-java-openshift",
			Tag:         "1.0",
		},
	},
	Tags: tags{
		Postgresql:    "9.5",
		Syndesis:      "1.1",
		Atlasmap:      "1.1",
		OAuthProxy:    "v1.1.0",
	},
	Registry: "registry.fuse-ignite.openshift.com",
}

var context = syndesisContext

func init() {
	flags := installCommand.PersistentFlags()

	flags.StringVar(&context.Name, "name", "syndesis", "Name of the template")
	flags.BoolVar(&context.AllowLocalHost, "allow-localhost", false, "Allow localhost")
	flags.BoolVar(&context.WithDockerImages, "with-docker-images", false, "With docker images")
	flags.BoolVar(&context.Restricted, "restricted", false, "Restricted mode?")
	flags.BoolVar(&context.Ephemeral, "ephemeral", false, "Ephemeral mode?")
	flags.BoolVar(&context.Probeless, "probeless", false, "Without probes")
	flags.StringVar(&context.Tags.Syndesis, "syndesis-tag", "latest", "Syndesis Image tag to use")
	flags.StringVar(&context.Tags.Atlasmap, "atlasmap-tag", "latest", "Atlasmap image to use")
	flags.BoolVar(&context.Productized, "product", false, "Generate product templates?")
	flags.StringVar(&context.Registry, "registry", "docker.io", "Registry to use for imagestreams")
	flags.BoolVar(&context.Debug, "debug", false, "Enable debug support")
	pflag.CommandLine.AddGoFlagSet(flag.CommandLine)
}

func install(cmd *cobra.Command, args []string) {

	if context.Productized {
		if err := mergo.MergeWithOverwrite(&context, productContext); err != nil {
			log.Fatal("Cannot merge in product image names")
		}
		context.Name = context.Name + "-" + context.Tags.Syndesis
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
