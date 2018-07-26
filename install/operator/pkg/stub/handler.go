package stub

import (
	"context"

	"github.com/operator-framework/operator-sdk/pkg/sdk"
	api "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis"
)

func NewHandler() sdk.Handler {
	return &Handler{}
}

type Handler struct {
	// Fill me
}

func (h *Handler) Handle(ctx context.Context, event sdk.Event) error {
	switch o := event.Object.(type) {
	case *api.Syndesis:
		return syndesis.Reconcile(o, event.Deleted)
	}
	return nil
}