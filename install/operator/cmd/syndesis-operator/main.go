package main

import (
	"context"
	"fmt"
	"github.com/syndesisio/syndesis/install/operator/pkg/openshift"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"os"
	"runtime"

	"github.com/operator-framework/operator-sdk/pkg/k8sutil"
	"github.com/operator-framework/operator-sdk/pkg/leader"
	"github.com/operator-framework/operator-sdk/pkg/log/zap"
	sdkVersion "github.com/operator-framework/operator-sdk/version"

	"github.com/spf13/pflag"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis"
	"github.com/syndesisio/syndesis/install/operator/pkg/controller"

	_ "k8s.io/client-go/plugin/pkg/client/auth/gcp"

	"sigs.k8s.io/controller-runtime/pkg/client/config"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
	"sigs.k8s.io/controller-runtime/pkg/runtime/signals"
)

var log = logf.Log.WithName("cmd")

func printVersion() {
	log.Info(fmt.Sprintf("Go Version: " + runtime.Version()))
	log.Info(fmt.Sprintf("Go OS/Arch: " + runtime.GOOS + "/" + runtime.GOARCH))
	log.Info(fmt.Sprintf("Version of operator-sdk: " + sdkVersion.Version))
}

func main() {
	configuration.TemplateLocation = pflag.StringP("template", "t", "/conf/syndesis-template.yml", "Path to template used for installation")
	configuration.AddonsDirLocation = pflag.StringP("addons", "a", "", "Path to the addons directory used for installation")
	configuration.Registry = pflag.StringP("registry", "r", "docker.io", "Registry to use for loading images like the upgrade pod")

	// The logger instantiated here can be changed to any logger
	// implementing the logr.Logger interface. This logger will
	// be propagated through the whole operator, generating
	// uniform and structured logs.
	pflag.CommandLine.AddFlagSet(zap.FlagSet())
	logf.SetLogger(zap.Logger())
	logf.SetLogger(logf.ZapLogger(false))

	pflag.Parse()

	log.Info("Using template", "template", *configuration.TemplateLocation)
	printVersion()

	namespace, err := k8sutil.GetWatchNamespace()
	if err != nil {
		log.Error(err, "Failed to get watch namespace")
		os.Exit(1)
	}

	// Get a config to talk to the apiserver
	cfg, err := config.GetConfig()
	if err != nil {
		log.Error(err, "")
		os.Exit(1)
	}

	// Become the leader before proceeding
	err = leader.Become(context.TODO(), "syndesis-operator-lock")
	if err != nil {
		log.Error(err, "")
		os.Exit(1)
	}

	// Create a new Cmd to provide shared dependencies and start components
	mgr, err := manager.New(cfg, manager.Options{Namespace: namespace})
	if err != nil {
		log.Error(err, "")
		os.Exit(1)
	}

	log.Info("Registering Components.")

	// Setup Scheme for all own CR resources
	if err := apis.AddToScheme(mgr.GetScheme()); err != nil {
		log.Error(err, "")
		os.Exit(1)
	}

	// Add OpenShift schema
	openshift.AddToScheme(mgr.GetScheme())

	// Setup all Controllers
	if err := controller.AddToManager(mgr); err != nil {
		log.Error(err, "")
		os.Exit(1)
	}

	log.Info("Starting the Cmd.")

	// Start the Cmd
	if err := mgr.Start(signals.SetupSignalHandler()); err != nil {
		log.Error(err, "Manager exited non-zero")
		os.Exit(1)
	}
}
