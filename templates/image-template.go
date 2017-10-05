package main

import (
	"flag"
	"fmt"
	"io/ioutil"
	// "log"
	//"sort"
	"strings"

	"github.com/hoisie/mustache"
	// "github.com/imdario/mergo"
	"github.com/spf13/cobra"
	"github.com/spf13/pflag"
	"os"
	//"path"
	"path/filepath"
	"io"
)

func main() {
	err := installCommand.Execute()
	check(err)
}

var installCommand = &cobra.Command{
	Use:   "image-template",
	Short: "image-template is a tool for creating the OpenShift templates used to install Syndesis.",
	Long:  `image-template is a tool for creating the OpenShift templates used to install Syndesis.`,
	Run:   install,
}

type Context struct {
	From string
}

var target = ""

var context = Context{
	From : "",
}

func init() {
	flags := installCommand.PersistentFlags()
	flags.StringVar(&target, "target", "centos", "Target directory")
	flags.StringVar(&context.From, "from", "fabric8/s2i-java:latest", "Name of the from image")
	pflag.CommandLine.AddGoFlagSet(flag.CommandLine)
}

func processDir(srcDir string, targetDir string) {

	fmt.Printf("Processing: %s => %s\n", srcDir, targetDir)
	files, err := ioutil.ReadDir(srcDir)
	check(err)
	check(err)

	for _, f := range files {

		srcName := filepath.Join(srcDir, f.Name());
		targetName := filepath.Join(targetDir, f.Name());

		fi, err := os.Stat(srcName)
		if err != nil {
			fmt.Println(err)
			return
		}

		switch mode := fi.Mode(); {
		case mode.IsDir():

			os.Mkdir(targetName, fi.Mode())
			processDir(srcName, targetName)

		case mode.IsRegular():

			if strings.HasSuffix(f.Name(), ".mustache") {
				targetName = strings.TrimSuffix(targetName, ".mustache")
				template, err := ioutil.ReadFile(srcName)
				check(err)
				rendered := []byte((mustache.Render(string(template), context)))
				err = ioutil.WriteFile(targetName, rendered, fi.Mode())
				check(err)
			} else {
				srcFile, err := os.Open(srcName)
				check(err)
				defer srcFile.Close()
				destFile, err := os.Create(targetName)
				check(err)
				defer destFile.Close()
				_, err = io.Copy(destFile, srcFile)
				check(err)
			}
		}

	}
}

func install(cmd *cobra.Command, args []string) {
	processDir("image", target);
}

func check(e error) {
	if e != nil {
		panic(e)
	}
}
