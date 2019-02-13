package template

import (
	"errors"
	v1template "github.com/openshift/api/template/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/apimachinery/pkg/util/json"
	"k8s.io/client-go/rest"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
)

var log = logf.Log.WithName("template")

type TemplateProcessor struct {
	namespace  string
	restClient *rest.RESTClient
}

func NewTemplateProcessor(namespace string) (*TemplateProcessor, error) {
	inConfig := rest.Config{}
	config := rest.CopyConfig(&inConfig)
	config.GroupVersion = &schema.GroupVersion{
		Group:   "template.openshift.io",
		Version: "v1",
	}
	config.APIPath = "/apis"
	config.AcceptContentTypes = "application/json"
	config.ContentType = "application/json"

	// this gets used for discovery and error handling types
	config.NegotiatedSerializer = basicNegotiatedSerializer{}
	if config.UserAgent == "" {
		config.UserAgent = rest.DefaultKubernetesUserAgent()
	}

	restClient, err := rest.RESTClientFor(config)
	if err != nil {
		return nil, err
	}

	return &TemplateProcessor{
		namespace:  namespace,
		restClient: restClient,
	}, nil
}

func (p *TemplateProcessor) Process(sourceTemplate *v1template.Template, parameters map[string]string) ([]runtime.RawExtension, error) {
	p.fillInParameters(sourceTemplate, parameters)

	resource, err := json.Marshal(sourceTemplate)
	if err != nil {
		return nil, err
	}

	result := p.restClient.
		Post().
		Namespace(p.namespace).
		Body(resource).
		Resource("processedtemplates").
		Do()

	if result.Error() == nil {
		data, err := result.Raw()
		if err != nil {
			return nil, err
		}

		templ, err := util.LoadResourceFromYaml(data)
		if err != nil {
			return nil, err
		}

		if v1Temp, ok := templ.(*v1template.Template); ok {
			return v1Temp.Objects, nil
		}
		log.Error(nil,"Wrong type returned by the server", "template", templ)
		return nil, errors.New("wrong type returned by the server")
	}

	return nil, result.Error()
}

func (p *TemplateProcessor) fillInParameters(template *v1template.Template, parameters map[string]string) {
	for i, param := range template.Parameters {
		if value, ok := parameters[param.Name]; ok {
			template.Parameters[i].Value = value
		}
	}
}
