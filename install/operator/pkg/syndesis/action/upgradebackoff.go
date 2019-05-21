package action

import (
	"context"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"k8s.io/client-go/kubernetes"
	"math"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	"strconv"
	"time"
)

const (
	// Number of times a syndesis upgrade will be triggered (including the first one launched by another state)
	UpgradeMaxAttempts = 5
)

// After a failure, waits a exponential amount of time, then retries
type upgradeBackoffAction struct {
	baseAction
	operatorVersion string
}

func newUpgradeBackoffAction(mgr manager.Manager, api kubernetes.Interface) SyndesisOperatorAction {
	return &upgradeBackoffAction{
		newBaseAction(mgr, api, "upgrade-backoff"),
		"",
	}
}

func (a *upgradeBackoffAction) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis, v1alpha1.SyndesisPhaseUpgradeFailureBackoff)
}

func (a *upgradeBackoffAction) Execute(ctx context.Context, syndesis *v1alpha1.Syndesis) error {

	// Check number of attempts to fail fast
	if syndesis.Status.UpgradeAttempts >= UpgradeMaxAttempts {
		a.log.Error(nil, "Upgrade of Syndesis resource failed too many times and will not be retried", "name", syndesis.Name)

		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseUpgradeFailed
		target.Status.Reason = v1alpha1.SyndesisStatusReasonTooManyUpgradeAttempts
		target.Status.Description = "Upgrade failed too many times and will not be retried"
		target.Status.ForceUpgrade = false

		return a.client.Update(ctx, target)
	}

	now := time.Now()

	lastFailureWrapper := syndesis.Status.LastUpgradeFailure
	var lastFailure time.Time
	if lastFailureWrapper != nil {
		lastFailure = lastFailureWrapper.Time
	} else {
		lastFailure = time.Now().Add(-8 * time.Hour)
	}

	if lastFailure.After(now) {
		lastFailure = now
	}

	power := float64(syndesis.Status.UpgradeAttempts - 1)
	if power < 0 {
		power = 0
	}

	delay := time.Duration(math.Pow(2, power)) * time.Minute

	nextAttempt := lastFailure.Add(delay)

	if now.After(nextAttempt) {
		a.log.Info("Restarting upgrade process for Syndesis resource", "name", syndesis.Name)

		currentVersion := syndesis.Status.Version
		targetVersion := syndesis.Status.TargetVersion
		currentAttemptStr := strconv.Itoa(int(syndesis.Status.UpgradeAttempts + 1))

		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseUpgrading
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = "Upgrading from " + currentVersion + " to " + targetVersion + " (attempt " + currentAttemptStr + ")"
		target.Status.ForceUpgrade = true

		return a.client.Update(ctx, target)
	} else {
		remaining := math.Round(nextAttempt.Sub(now).Seconds())
		a.log.Info("Upgrade of Syndesis resource will be retried", "name", syndesis.Name, "retryAfterSeconds", remaining)
		return nil
	}
}
