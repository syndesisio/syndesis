package syndesis

import (
	"fmt"
	"github.com/operator-framework/operator-sdk/pkg/k8sclient"
	"github.com/operator-framework/operator-sdk/pkg/sdk"
	"github.com/sirupsen/logrus"
	api "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/action"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

var (
	actionPool []action.InstallationAction
)

func init() {
	actionPool = append(actionPool,
		&action.UpgradeLegacy{},
		&action.Initialize{},
		&action.Install{},
		&action.Startup{},
		&action.CheckUpdates{},
		&action.Upgrade{},
		&action.UpgradeBackoff{},
	)
}

// Reconcile the state of the Syndesis infrastructure elements
func Reconcile(syndesis *api.Syndesis, deleted bool) error {

	if deleted {
		if err := deleteOAuthClient(syndesis); err != nil {
			return err
		}

		logrus.Info("Syndesis resource ", syndesis.Name, " deleted")
		return nil
	}

	// Don't want to do anything if the syndesis resource has been updated in the meantime
	// This happens when a processing takes more tha the resync period
	if latest, err := isLatestVersion(syndesis); err != nil || !latest {
		return err
	}

	for _, a := range actionPool {
		if a.CanExecute(syndesis) {
			if err := a.Execute(syndesis); err != nil {
				return err
			}
		}
	}

	return nil
}

func isLatestVersion(syndesis *api.Syndesis) (bool, error) {
	refreshed := syndesis.DeepCopy()
	if err := sdk.Get(refreshed); err != nil {
		return false, err
	}
	return refreshed.ResourceVersion == syndesis.ResourceVersion, nil
}

func deleteOAuthClient(syndesis *api.Syndesis) error {
	resourceClient, _, err := k8sclient.GetResourceClient("oauth.openshift.io/v1", "OAuthClient", "")
	if err != nil {
		return fmt.Errorf("failed to get resource client: %v", err)
	}

	name := "syndesis-" + syndesis.Namespace
	obj, err := resourceClient.Get(name, metav1.GetOptions{})
	if err != nil {
		logrus.Warn("unable to get OAuthClient object named `", name, "` ", err)
		return nil
	}

	if obj == nil {
		return nil
	}

	if err := resourceClient.Delete(name, nil); err != nil {
		return err
	}

	return nil
}
