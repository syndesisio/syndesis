package template

import (
	"errors"
	v1template "github.com/openshift/api/template/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/apimachinery/pkg/util/json"
	"k8s.io/client-go/rest"
	"sigs.k8s.io/controller-runtime/pkg/client/config"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
)

var log = logf.Log.WithName("template")

type TemplateProcessor struct {
	namespace  string
	restClient *rest.RESTClient
	scheme     *runtime.Scheme
}

func NewTemplateProcessor(scheme *runtime.Scheme, namespace string) (*TemplateProcessor, error) {
	inConfig, err := config.GetConfig()
	if err != nil {
		return nil, err
	}
	config := rest.CopyConfig(inConfig)
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
		scheme:     scheme,
	}, nil
}

func (p *TemplateProcessor) Process(sourceTemplate *v1template.Template, parameters map[string]string) ([]runtime.RawExtension, error) {
	p.fillInParameters(sourceTemplate, parameters)

	resource, err := json.Marshal(sourceTemplate)
	if err != nil {
		return nil, err
	}

	log.V(4).Info("Template with parameters", "template", resource)

	result := p.restClient.
		Post().
		Namespace(p.namespace).
		Body(resource).
		Resource("processedtemplates").
		Do()

	if result.Error() != nil {
		return nil, result.Error()
	}

	data, err := result.Raw()
	if err != nil {
		return nil, err
	}

	templ, err := util.LoadResourceFromYaml(p.scheme, data)
	if err != nil {
		return nil, err
	}

	if v1Temp, ok := templ.(*v1template.Template); ok {
		return v1Temp.Objects, nil
	}
	log.Error(nil, "Wrong type returned by the server", "template", templ)
	return nil, errors.New("wrong type returned by the server")
}

func (p *TemplateProcessor) fillInParameters(template *v1template.Template, parameters map[string]string) {
	for i, param := range template.Parameters {
		if value, ok := parameters[param.Name]; ok {
			template.Parameters[i].Value = value
		}
	}
}
