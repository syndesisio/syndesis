package legacy

import (
	"context"
	"github.com/operator-framework/operator-sdk/pkg/sdk"
	"github.com/sirupsen/logrus"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"time"
)

const (
	retryInterval = 5 * time.Second
	syndesisResourceName = "syndesis-app"
)

// Checks for an existing legacy Syndesis installation and creates a Syndesis CR that takes control of it.
// Verification is done only once at startup. In case of error, the verification will be tried again at periodic intervals.
type LegacyController struct {
	namespace	string
}

func NewLegacyController(namespace string) *LegacyController {
	return &LegacyController{
		namespace: namespace,
	}
}

func (c *LegacyController) Start(ctx context.Context) {
	go c.verifyAndCreate(ctx)
}

func (c *LegacyController) verifyAndCreate(ctx context.Context) {
	defer logrus.Info("Syndesis legacy installations check completed")

	err := c.doVerifyAndCreate()
	if err != nil {
		logrus.Error("Unable to check Syndesis legacy installations (will retry again): ", err)

		for {
			select {
			case <-ctx.Done():
				return
			case <-time.After(retryInterval):
				if err := c.doVerifyAndCreate(); err != nil {
					logrus.Error("Unable to check Syndesis legacy installations (will retry again): ", err)
				} else {
					return
				}
			}
		}
	}
}

func (c *LegacyController) doVerifyAndCreate() error {
	if exists, err := c.legacyInstallationExists(); err != nil {
		return err
	} else if exists {
		logrus.Info("A legacy Syndesis installations is present in the ", c.namespace, " namespace")

		synd := v1alpha1.Syndesis{
			TypeMeta: metav1.TypeMeta{
				Kind: "Syndesis",
				APIVersion: v1alpha1.SchemeGroupVersion.String(),
			},
			ObjectMeta: metav1.ObjectMeta{
				Namespace: c.namespace,
				Name: syndesisResourceName,
			},
			Status: v1alpha1.SyndesisStatus{
				Phase: v1alpha1.SyndesisPhaseUpgradingLegacy,
				Reason: v1alpha1.SyndesisStatusReasonMissing,
			},
		}

		logrus.Info("Merging Syndesis legacy configuration into resource ", syndesisResourceName)

		config, err := configuration.GetSyndesisEnvVarsFromOpenshiftNamespace(c.namespace)
		if err != nil {
			return nil
		}

		configuration.SetConfigurationFromEnvVars(config, &synd)

		logrus.Info("Creating a new Syndesis resource from legacy installation in the ", c.namespace, " namespace")
		return sdk.Create(&synd)
	} else {
		logrus.Info("No legacy Syndesis installations detected in the ", c.namespace, " namespace")
		return nil
	}
}

func (c *LegacyController) legacyInstallationExists() (bool, error) {
	// There exists a Syndesis configuration
	v, err := configuration.GetSyndesisVersionFromNamespace(c.namespace)
	if err != nil && k8serrors.IsNotFound(err) {
		return false, nil
	} else if err != nil || v == "" {
		return false, err
	}

	// There's not any Syndesis resource
	lst := v1alpha1.NewSyndesisList()
	err = sdk.List(c.namespace, lst)
	if err != nil || len(lst.Items) > 0 {
		return false, err
	}

	return true, nil
}