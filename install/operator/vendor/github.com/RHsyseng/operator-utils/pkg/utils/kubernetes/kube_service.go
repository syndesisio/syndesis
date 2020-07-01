package kubernetes

import (
	"context"
	"github.com/RHsyseng/operator-utils/pkg/logs"

	imagev1 "github.com/openshift/client-go/image/clientset/versioned/typed/image/v1"
	"k8s.io/apimachinery/pkg/runtime"
	cachev1 "sigs.k8s.io/controller-runtime/pkg/cache"
	clientv1 "sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

var log = logs.GetLogger("operatorutils.kubernetes")

type KubernetesPlatformService struct {
	client      clientv1.Client
	cache       cachev1.Cache
	imageClient *imagev1.ImageV1Client
	scheme      *runtime.Scheme
}

func GetInstance(mgr manager.Manager) KubernetesPlatformService {
	imageClient, err := imagev1.NewForConfig(mgr.GetConfig())
	if err != nil {
		log.Errorf("Error getting image client: %v", err)
		return KubernetesPlatformService{}
	}

	return KubernetesPlatformService{
		client:      mgr.GetClient(),
		cache:       mgr.GetCache(),
		imageClient: imageClient,
		scheme:      mgr.GetScheme(),
	}
}

func (service *KubernetesPlatformService) Create(ctx context.Context, obj runtime.Object, opts ...clientv1.CreateOption) error {
	return service.client.Create(ctx, obj, opts...)
}

func (service *KubernetesPlatformService) Delete(ctx context.Context, obj runtime.Object, opts ...clientv1.DeleteOption) error {
	return service.client.Delete(ctx, obj, opts...)
}

func (service *KubernetesPlatformService) Get(ctx context.Context, key clientv1.ObjectKey, obj runtime.Object) error {
	return service.client.Get(ctx, key, obj)
}

func (service *KubernetesPlatformService) List(ctx context.Context, list runtime.Object, opts ...clientv1.ListOption) error {
	return service.client.List(ctx, list, opts...)
}

func (service *KubernetesPlatformService) Update(ctx context.Context, obj runtime.Object, opts ...clientv1.UpdateOption) error {
	return service.client.Update(ctx, obj, opts...)
}

func (service *KubernetesPlatformService) Patch(ctx context.Context, obj runtime.Object, patch clientv1.Patch, opts ...clientv1.PatchOption) error {
	return service.client.Patch(ctx, obj, patch, opts...)
}

func (service *KubernetesPlatformService) DeleteAllOf(ctx context.Context, obj runtime.Object, opts ...clientv1.DeleteAllOfOption) error {
	return service.client.DeleteAllOf(ctx, obj, opts...)
}

func (service *KubernetesPlatformService) GetCached(ctx context.Context, key clientv1.ObjectKey, obj runtime.Object) error {
	return service.cache.Get(ctx, key, obj)
}

func (service *KubernetesPlatformService) ImageStreamTags(namespace string) imagev1.ImageStreamTagInterface {
	return service.imageClient.ImageStreamTags(namespace)
}

func (service *KubernetesPlatformService) GetScheme() *runtime.Scheme {
	return service.scheme
}

func (service *KubernetesPlatformService) IsMockService() bool {
	return false
}
