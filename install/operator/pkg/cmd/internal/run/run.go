/*
 * Copyright (C) 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package run

import (
	"context"
	"fmt"
	"github.com/prometheus/client_golang/prometheus"
	"k8s.io/client-go/rest"
	"runtime"

	kubemetrics "github.com/operator-framework/operator-sdk/pkg/kube-metrics"

	customMetrics "sigs.k8s.io/controller-runtime/pkg/metrics"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/versions"
	v1 "k8s.io/api/core/v1"

	"github.com/syndesisio/syndesis/install/operator/pkg"

	"github.com/operator-framework/operator-sdk/pkg/k8sutil"
	"github.com/operator-framework/operator-sdk/pkg/leader"
	"github.com/operator-framework/operator-sdk/pkg/log/zap"
	"github.com/operator-framework/operator-sdk/pkg/metrics"
	"github.com/pkg/errors"
	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client/config"
	"sigs.k8s.io/controller-runtime/pkg/manager"

	consolev1 "github.com/openshift/api/console/v1"
	sdkVersion "github.com/operator-framework/operator-sdk/version"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis"
	"github.com/syndesisio/syndesis/install/operator/pkg/controller"
	"github.com/syndesisio/syndesis/install/operator/pkg/openshift"
	logf "sigs.k8s.io/controller-runtime/pkg/log"

	"sigs.k8s.io/controller-runtime/pkg/manager/signals"
)

var (
	metricsHost               = "0.0.0.0"
	metricsPort         int32 = 8383
	operatorMetricsPort int32 = 8686
)

// Custom metrics
var (
	operatorVersion = prometheus.NewGauge(
		prometheus.GaugeOpts{
			Name:        "syndesis_version_info",
			Help:        "Syndesis operator information",
			ConstLabels: prometheus.Labels{"operator_version": pkg.DefaultOperatorTag},
		},
	)
)

func init() {
	// Register custom metrics with the global prometheus registry
	customMetrics.Registry.MustRegister(operatorVersion)
}

var log = logf.Log.WithName("cmd")

func printVersion() {
	log.Info(fmt.Sprintf("Go Version: %s", runtime.Version()))
	log.Info(fmt.Sprintf("Go OS/Arch: %s/%s", runtime.GOOS, runtime.GOARCH))
	log.Info(fmt.Sprintf("Version of operator-sdk: %v", sdkVersion.Version))
	log.Info(fmt.Sprintf("Syndesis Operator Version: %s", pkg.DefaultOperatorTag))
	log.Info(fmt.Sprintf("Syndesis Operator Image: %s", pkg.DefaultOperatorImage))
	if len(pkg.BuildDateTime) > 0 {
		log.Info(fmt.Sprintf("Syndesis Build Time: %s", pkg.BuildDateTime))
	}
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

	syndesis := &v1beta1.Syndesis{}
	syndesis.SetNamespace(namespace)
	config, err := configuration.GetProperties(o.Context, configuration.TemplateConfig, o.ClientTools(), syndesis)
	if err != nil {
		return err
	}

	util.KnownDockerImages[config.Syndesis.Components.Server.Image] = true
	util.KnownDockerImages[config.Syndesis.Components.Meta.Image] = true
	util.KnownDockerImages[config.Syndesis.Components.UI.Image] = true
	util.KnownDockerImages[config.Syndesis.Components.S2I.Image] = true
	util.KnownDockerImages[config.Syndesis.Components.Database.Image] = true
	util.KnownDockerImages[config.Syndesis.Components.Oauth.Image] = true
	util.KnownDockerImages[config.Syndesis.Components.Database.Exporter.Image] = true
	util.KnownDockerImages[config.Syndesis.Components.Prometheus.Image] = true
	util.KnownDockerImages[config.Syndesis.Components.Upgrade.Image] = true

	ctx := o.Context

	// Become the leader before proceeding
	err = leader.Become(ctx, "syndesis-operator-lock")
	if err != nil {
		return err
	}

	// Create a new Cmd to provide shared dependencies and start components
	mgr, err := manager.New(o.ClientTools().RestConfig(), manager.Options{
		Namespace:          namespace,
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

	if err := consolev1.Install(mgr.GetScheme()); err != nil {
		return err
	}

	cli, err := o.ClientTools().RuntimeClient()
	if err != nil {
		return err
	}

	am, err := versions.APIMigrator(o.Context, cli, namespace)
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

    // Add the Metrics Service
    addMetrics(ctx, cfg, namespace)

	if err := mgr.Start(signals.SetupSignalHandler()); err != nil {
		return err
	}

	return nil
}

// addMetrics will create the Services and Service Monitors to allow the operator export the metrics by using
// the Prometheus operator
func addMetrics(ctx context.Context, cfg *rest.Config, namespace string) {
    if err := serveCRMetrics(cfg); err != nil {
        if errors.Is(err, k8sutil.ErrRunLocal) {
            log.Info("Skipping CR metrics server creation; not running in a cluster.")
            return
        }
        log.Info("Could not generate and serve custom resource metrics", "error", err.Error())
    }

    // Add to the below struct any other metrics ports you want to expose.
    servicePorts := []v1.ServicePort{
        {Port: metricsPort, Name: metrics.OperatorPortName, Protocol: v1.ProtocolTCP, TargetPort: intstr.IntOrString{Type: intstr.Int, IntVal: metricsPort}},
        {Port: operatorMetricsPort, Name: metrics.CRPortName, Protocol: v1.ProtocolTCP, TargetPort: intstr.IntOrString{Type: intstr.Int, IntVal: operatorMetricsPort}},
    }

    // Create Service object to expose the metrics port(s).
    service, err := metrics.CreateMetricsService(ctx, cfg, servicePorts)
    if err != nil {
        log.Info("Could not create metrics Service", "error", err.Error())
    }

    // CreateServiceMonitors will automatically create the prometheus-operator ServiceMonitor resources
    // necessary to configure Prometheus to scrape metrics from this operator.
    services := []*v1.Service{service}
    _, err = metrics.CreateServiceMonitors(cfg, namespace, services)
    if err != nil {
        log.Info("Could not create ServiceMonitor object", "error", err.Error())
        // If this operator is deployed to a cluster without the prometheus-operator running, it will return
        // ErrServiceMonitorNotPresent, which can be used to safely skip ServiceMonitor creation.
        if err == metrics.ErrServiceMonitorNotPresent {
            log.Info("Install prometheus-operator in your cluster to create ServiceMonitor objects", "error", err.Error())
        }
    }
}

// serveCRMetrics gets the Operator/CustomResource GVKs and generates metrics based on those types.
// It serves those metrics on "http://metricsHost:operatorMetricsPort".
func serveCRMetrics(cfg *rest.Config) error {
    // Below function returns filtered operator/CustomResource specific GVKs.
    // For more control override the below GVK list with your own custom logic.
    filteredGVK, err := k8sutil.GetGVKsFromAddToScheme(apis.AddToScheme)
    if err != nil {
        return err
    }
    // Get the namespace the operator is currently deployed in.
    operatorNs, err := k8sutil.GetOperatorNamespace()
    if err != nil {
        return err
    }
    // To generate metrics in other namespaces, add the values below.
    ns := []string{operatorNs}
    // Generate and serve custom resource specific metrics.
    err = kubemetrics.GenerateAndServeCRMetrics(cfg, ns, filteredGVK, metricsHost, operatorMetricsPort)
    if err != nil {
        return err
    }
    return nil
}
