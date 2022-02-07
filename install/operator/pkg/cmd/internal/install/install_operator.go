package install

import (
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg/util"
)

func (o *Install) installOperatorResources() error {
	resources, err := o.render("assets/install/role.yml.tmpl")
	if err != nil {
		return err
	}

	oinstall, err := o.render("assets/install/operator_install.yml.tmpl")
	if err != nil {
		return err
	}
	resources = append(resources, oinstall...)

	deployment, err := o.render("assets/install/operator_deployment.yml.tmpl")
	if err != nil {
		return err
	}
	resources = append(resources, deployment...)

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

		client, err := o.ClientTools().DynamicClient()
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
