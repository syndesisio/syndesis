package install

import (
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"time"
)

func (o *Install) installOperatorResources() error {
	resources, err := o.render("./install/role.yml.tmpl")
	if err != nil {
		return err
	}

	operator, err := o.render("./install/operator.yml.tmpl")
	if err != nil {
		return err
	}
	resources = append(resources, operator...)

	if o.ejectedResources != nil {
		o.ejectedResources = append(o.ejectedResources, resources...)
	} else {
		for _, res := range resources {
			res.SetNamespace(o.Namespace)
		}
		err := o.install("operator was", resources)
		if err != nil {
			return err
		}

		client, err := o.NewDynamicClient()
		if err != nil {
			return err
		}

		_, err = util.WaitForDeploymentReady(o.Context, client, o.Namespace, "syndesis-operator", 2*time.Second)
		if err != nil {
			return err
		}
	}

	return err
}
