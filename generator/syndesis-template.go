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

type Context struct {
	Name       string
	Dev        bool
	Ephemeral  bool
	Restricted bool
}

var context = Context{}

func init() {
	installCommand.PersistentFlags().StringVar(&context.Name, "name", "syndesis", "Name of the template")
	installCommand.PersistentFlags().BoolVar(&context.Dev, "dev", false, "Developer mode?")
	installCommand.PersistentFlags().BoolVar(&context.Restricted, "restricted", false, "Restricted mode?")
	installCommand.PersistentFlags().BoolVar(&context.Ephemeral, "ephemeral", false, "Ephemeral mode?")
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
