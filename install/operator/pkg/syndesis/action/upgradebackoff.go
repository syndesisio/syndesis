package action

import (
	"context"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"math"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"strconv"
	"time"
)

const (
	// Number of times a syndesis upgrade will be triggered (including the first one launched by another state)
	UpgradeMaxAttempts = 5
)

// After a failure, waits a exponential amount of time, then retries
type UpgradeBackoff struct {
	operatorVersion string
}

func (a *UpgradeBackoff) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis, v1alpha1.SyndesisPhaseUpgradeFailureBackoff)
}

func (a *UpgradeBackoff) Execute(client client.Client, syndesis *v1alpha1.Syndesis) error {

	// Check number of attempts to fail fast
	if syndesis.Status.UpgradeAttempts >= UpgradeMaxAttempts {
		log.Error(nil,"Upgrade of Syndesis resource failed too many times and will not be retried", "name", syndesis.Name, "type", "backoff")

		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseUpgradeFailed
		target.Status.Reason = v1alpha1.SyndesisStatusReasonTooManyUpgradeAttempts
		target.Status.Description = "Upgrade failed too many times and will not be retried"
		target.Status.ForceUpgrade = false

		return client.Update(context.TODO(), target)
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
		log.Info("Restarting upgrade process for Syndesis resource", "name", syndesis.Name, "type", "backoff")

		currentVersion := syndesis.Status.Version
		targetVersion := syndesis.Status.TargetVersion
		currentAttemptStr := strconv.Itoa(int(syndesis.Status.UpgradeAttempts + 1))

		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseUpgrading
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = "Upgrading from " + currentVersion + " to " + targetVersion + " (attempt " + currentAttemptStr + ")"
		target.Status.ForceUpgrade = true

		return client.Update(context.TODO(), target)
	} else {
		remaining := math.Round(nextAttempt.Sub(now).Seconds())
		log.Info("Upgrade of Syndesis resource will be retried", "name", syndesis.Name, "retryAfterSeconds", remaining, "type", "backoff")
		return nil
	}
}
