package run

import (
	"fmt"
	"runtime"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/versions"

	v1 "k8s.io/api/core/v1"

	"github.com/syndesisio/syndesis/install/operator/pkg"

	"github.com/operator-framework/operator-sdk/pkg/k8sutil"
	"github.com/operator-framework/operator-sdk/pkg/leader"
	"github.com/operator-framework/operator-sdk/pkg/log/zap"
	"github.com/operator-framework/operator-sdk/pkg/metrics"
	"github.com/operator-framework/operator-sdk/pkg/restmapper"
	"github.com/pkg/errors"
	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client/config"
	"sigs.k8s.io/controller-runtime/pkg/manager"

	sdkVersion "github.com/operator-framework/operator-sdk/version"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis"
	"github.com/syndesisio/syndesis/install/operator/pkg/controller"
	"github.com/syndesisio/syndesis/install/operator/pkg/openshift"
	logf "sigs.k8s.io/controller-runtime/pkg/log"

	"sigs.k8s.io/controller-runtime/pkg/manager/signals"
)

var (
	metricsHost       = "0.0.0.0"
	metricsPort int32 = 8383
)
var log = logf.Log.WithName("cmd")

func printVersion() {
	log.Info(fmt.Sprintf("Go Version: %s", runtime.Version()))
	log.Info(fmt.Sprintf("Go OS/Arch: %s/%s", runtime.GOOS, runtime.GOARCH))
	log.Info(fmt.Sprintf("Version of operator-sdk: %v", sdkVersion.Version))
	log.Info(fmt.Sprintf("Syndesis Operator Version: %s", pkg.DefaultOperatorTag))
	log.Info(fmt.Sprintf("Syndesis Operator Image: %s", pkg.DefaultOperatorImage))
}

func New(parent *internal.Options) *cobra.Command {

	options := options{Options: parent}
	cmd := cobra.Command{
		Use:   "run",
		Short: "runs the operator",
		Run: func(_ *cobra.Command, _ []string) {
			util.ExitOnError(options.run())
		},
	}

	cmd.PersistentFlags().StringVarP(&configuration.TemplateConfig, "operator-config", "", "/conf/config.yaml", "Path to the operator configuration file.")
	cmd.PersistentFlags().AddFlagSet(zap.FlagSet())
	cmd.PersistentFlags().AddFlagSet(util.FlagSet)

	return &cmd
}

type options struct {
	*internal.Options
}

func (o *options) run() error {
	logf.SetLogger(zap.Logger())

	printVersion()
	namespace, err := k8sutil.GetWatchNamespace()
	if err != nil {
		return errors.Wrap(err, "failed to get watch namespace")
	}

	// Get a config to talk to the apiserver
	cfg, err := config.GetConfig()
	if err != nil {
		return err
	}

	configuration, err := configuration.GetProperties(configuration.TemplateConfig, o.Context, nil, &v1beta1.Syndesis{})
	if err != nil {
		return err
	}

	util.KnownDockerImages[configuration.Syndesis.Components.Server.Image] = true
	util.KnownDockerImages[configuration.Syndesis.Components.Meta.Image] = true
	util.KnownDockerImages[configuration.Syndesis.Components.UI.Image] = true
	util.KnownDockerImages[configuration.Syndesis.Components.S2I.Image] = true
	util.KnownDockerImages[configuration.Syndesis.Components.Database.Image] = true
	util.KnownDockerImages[configuration.Syndesis.Components.Oauth.Image] = true
	util.KnownDockerImages[configuration.Syndesis.Components.Database.Exporter.Image] = true
	util.KnownDockerImages[configuration.Syndesis.Components.Prometheus.Image] = true
	util.KnownDockerImages[configuration.Syndesis.Components.Upgrade.Image] = true
	util.KnownDockerImages[configuration.Syndesis.Addons.DV.Image] = true

	ctx := o.Context

	// Become the leader before proceeding
	err = leader.Become(ctx, "syndesis-operator-lock")
	if err != nil {
		return err
	}

	// Create a new Cmd to provide shared dependencies and start components
	mgr, err := manager.New(cfg, manager.Options{
		Namespace:          namespace,
		MapperProvider:     restmapper.NewDynamicRESTMapper,
		MetricsBindAddress: fmt.Sprintf("%s:%d", metricsHost, metricsPort),
	})
	if err != nil {
		return err
	}

	log.Info("registering resource schemes.")
	// Setup Scheme for all resources
	if err := apis.AddToScheme(mgr.GetScheme()); err != nil {
		return err
	}

	openshift.AddToScheme(mgr.GetScheme())

	cli, err := o.GetClient()
	if err != nil {
		return err
	}

	am, err := versions.ApiMigrator(cli, o.Context, namespace)
	if err != nil {
		return err
	}

	if err = am.Migrate(); err != nil {
		return err
	}

	// Setup all Controllers
	if err := controller.AddToManager(mgr); err != nil {
		return err
	}

	// Create Service object to expose the metrics port.
	servicePorts := []v1.ServicePort{
		{Port: metricsPort, Name: metrics.OperatorPortName, Protocol: v1.ProtocolTCP, TargetPort: intstr.IntOrString{Type: intstr.Int, IntVal: metricsPort}},
		{Port: metricsPort, Name: metrics.CRPortName, Protocol: v1.ProtocolTCP, TargetPort: intstr.IntOrString{Type: intstr.Int, IntVal: metricsPort}},
	}

	_, err = metrics.CreateMetricsService(ctx, cfg, servicePorts)
	if err != nil {
		log.Info(err.Error())
	}

	if err := mgr.Start(signals.SetupSignalHandler()); err != nil {
		return err
	}
	return nil
}
