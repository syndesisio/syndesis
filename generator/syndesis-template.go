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
	PemToKeystore string
	TokenRp       string
	Keycloak      string
	Postgresql    string
}

type syndesisImages struct {
	Rest     string
	Ui       string
	Verifier string
	Atlasmap string
}

type images struct {
	Support  supportImages
	Syndesis syndesisImages
}

type tags struct {
	Keycloak      string
	TokenRp       string
	Syndesis      string
	Atlasmap      string
	Postgresql    string
	PemToKeystore string
}

type Context struct {
	Name        string
	Dev         bool
	Ephemeral   bool
	Restricted  bool
	Probeless   bool
	Productized bool
	Tag         string
	Registry    string
	Images      images
	Tags        tags
}

// TODO: Could be added from a local configuration file
var syndesisContext = Context{
	Images: images{
		Support: supportImages{
			PemToKeystore: "syndesis/pemtokeystore",
			TokenRp:       "syndesis/token-rp",
			Keycloak:      "jimmidyson/keycloak-openshift",
			Postgresql:    "postgresql",
		},
		Syndesis: syndesisImages{
			Rest:     "syndesis/syndesis-rest",
			Ui:       "syndesis/syndesis-ui",
			Verifier: "syndesis/syndesis-verifier",
			Atlasmap: "atlasmap/atlasmap",
		},
	},
	Tags: tags{
		Postgresql:    "9.5",
		PemToKeystore: "v0.2.1",
		Keycloak:      "2.5.4.Final",
		TokenRp:       "v0.6.2",
	},
}

// TODO: Update with product image references here
var productContext = Context{
	Images: images{
		Support: supportImages{
			PemToKeystore: "syndesis/pemtokeystore",
			TokenRp:       "syndesis/token-rp",
			Keycloak:      "jimmidyson/keycloak-openshift",
			Postgresql:    "postgresql",
		},
		Syndesis: syndesisImages{
			Rest:     "syndesis/syndesis-rest",
			Ui:       "syndesis/syndesis-ui",
			Verifier: "syndesis/syndesis-verifier",
			Atlasmap: "atlasmap/atlasmap",
		},
	},
	Tags: tags{
		Postgresql:    "9.5",
		PemToKeystore: "v0.2.1",
		Keycloak:      "2.5.4.Final",
		TokenRp:       "v0.6.2",
	},
}

var context = syndesisContext

func init() {
	flags := installCommand.PersistentFlags()

	flags.StringVar(&context.Name, "name", "syndesis", "Name of the template")
	flags.BoolVar(&context.Dev, "dev", false, "Developer mode?")
	flags.BoolVar(&context.Restricted, "restricted", false, "Restricted mode?")
	flags.BoolVar(&context.Ephemeral, "ephemeral", false, "Ephemeral mode?")
	flags.BoolVar(&context.Probeless, "probeless", false, "Without probes")
	flags.StringVar(&context.Tags.Syndesis, "syndesis", "latest", "Syndesis Image tag to use")
	flags.StringVar(&context.Tags.Atlasmap, "atlasmap", "latest", "Atlasmap image to use")
	flags.BoolVar(&context.Productized, "product", false, "Generate product templates?")
	flags.StringVar(&context.Registry, "registry", "docker.io", "Registry to use for imagestreams")
	pflag.CommandLine.AddGoFlagSet(flag.CommandLine)
}

func install(cmd *cobra.Command, args []string) {

	if context.Productized {
		if err := mergo.MergeWithOverwrite(&context, productContext); err != nil {
			log.Fatal("Cannot merge in product image names")
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
