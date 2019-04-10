package controller

import (
	"github.com/syndesisio/syndesis/install/operator/pkg/controller/syndesis"
)

func init() {
	// AddToManagerFuncs is a list of functions to create controllers and add them to a manager.
	AddToManagerFuncs = append(AddToManagerFuncs, syndesis.Add)
}
