package action

import (
	"context"
	"math"
	"strconv"
	"time"

	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"sigs.k8s.io/controller-runtime/pkg/manager"
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

func newUpgradeBackoffAction(mgr manager.Manager, clientTools *clienttools.ClientTools) SyndesisOperatorAction {
	return &upgradeBackoffAction{
		newBaseAction(mgr, clientTools, "upgrade-backoff"),
		"",
	}
}

func (a *upgradeBackoffAction) CanExecute(syndesis *synapi.Syndesis) bool {
	return syndesisPhaseIs(syndesis, synapi.SyndesisPhaseUpgradeFailureBackoff)
}

func (a *upgradeBackoffAction) Execute(ctx context.Context, syndesis *synapi.Syndesis, operatorNamespace string, productName string) error {
	rtClient, _ := a.clientTools.RuntimeClient()

	// Check number of attempts to fail fast
	if syndesis.Status.UpgradeAttempts >= UpgradeMaxAttempts {
		a.log.Error(nil, "Upgrade of Syndesis resource failed too many times and will not be retried", "name", syndesis.Name)

		target := syndesis.DeepCopy()
		target.Status.Phase = synapi.SyndesisPhaseUpgradeFailed
		target.Status.Reason = synapi.SyndesisStatusReasonTooManyUpgradeAttempts
		target.Status.Description = "Upgrade failed too many times and will not be retried"
		target.Status.ForceUpgrade = false

		return rtClient.Status().Update(ctx, target)
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
		target.Status.Phase = synapi.SyndesisPhaseUpgrading
		target.Status.Reason = synapi.SyndesisStatusReasonMissing
		target.Status.Description = "Upgrading from " + currentVersion + " to " + targetVersion + " (attempt " + currentAttemptStr + ")"
		target.Status.ForceUpgrade = true

		return rtClient.Status().Update(ctx, target)
	} else {
		remaining := math.Round(nextAttempt.Sub(now).Seconds())
		a.log.Info("Upgrade of Syndesis resource will be retried", "name", syndesis.Name, "retryAfterSeconds", remaining)
		return nil
	}
}
