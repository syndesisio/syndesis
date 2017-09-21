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
	"sort"
	"strings"

	"github.com/hoisie/mustache"
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

type images struct {
	PemToKeystore string
	TokenRp string
	Keycloak string
	Postgresql string
}

type tags struct {
	Keycloak string
	TokenRp string
	Syndesis string
}

type Context struct {
	Name       string
	Dev        bool
	Ephemeral  bool
	Restricted bool
	Probeless  bool
	Tag        string
	Registry   string
	Images     images
	Tags       tags
}

// TODO: Could be added from a local configuration file
var context = Context{
	Images: images{
	  PemToKeystore: "syndesis/pemtokeystore:v0.2.1",
	  TokenRp: "syndesis/token-rp:v0.6.2",
		Keycloak: "jimmidyson/keycloak-openshift:2.5.4.Final",
		Postgresql: "postgresql:9.5",
  },
	Tags: tags{
		Keycloak: "2.5.4.Final",
		TokenRp: "v0.6.2",
	},
}

func init() {
	flags := installCommand.PersistentFlags()
	flags.StringVar(&context.Name, "name", "syndesis", "Name of the template")
	flags.BoolVar(&context.Dev, "dev", false, "Developer mode?")
	flags.BoolVar(&context.Restricted, "restricted", false, "Restricted mode?")
	flags.BoolVar(&context.Ephemeral, "ephemeral", false, "Ephemeral mode?")
	flags.BoolVar(&context.Probeless, "probeless", false, "Without probes")
	flags.StringVar(&context.Tags.Syndesis,"tag", "latest", "Image tag to use")
	flags.StringVar(&context.Registry,"registry", "docker.io", "Registry to use for imagestreams")
	pflag.CommandLine.AddGoFlagSet(flag.CommandLine)
}

func install(cmd *cobra.Command, args []string) {

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
