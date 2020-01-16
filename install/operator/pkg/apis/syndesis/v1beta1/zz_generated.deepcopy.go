// +build !ignore_autogenerated

// Code generated by operator-sdk. DO NOT EDIT.

package v1beta1

import (
	runtime "k8s.io/apimachinery/pkg/runtime"
)

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *AddonSpec) DeepCopyInto(out *AddonSpec) {
	*out = *in
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new AddonSpec.
func (in *AddonSpec) DeepCopy() *AddonSpec {
	if in == nil {
		return nil
	}
	out := new(AddonSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *AddonsSpec) DeepCopyInto(out *AddonsSpec) {
	*out = *in
	out.Jaeger = in.Jaeger
	out.Ops = in.Ops
	out.Todo = in.Todo
	out.Knative = in.Knative
	out.DV = in.DV
	out.CamelK = in.CamelK
	out.PublicApi = in.PublicApi
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new AddonsSpec.
func (in *AddonsSpec) DeepCopy() *AddonsSpec {
	if in == nil {
		return nil
	}
	out := new(AddonsSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *BackupConfig) DeepCopyInto(out *BackupConfig) {
	*out = *in
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new BackupConfig.
func (in *BackupConfig) DeepCopy() *BackupConfig {
	if in == nil {
		return nil
	}
	out := new(BackupConfig)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *BackupStatus) DeepCopyInto(out *BackupStatus) {
	*out = *in
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new BackupStatus.
func (in *BackupStatus) DeepCopy() *BackupStatus {
	if in == nil {
		return nil
	}
	out := new(BackupStatus)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ComponentsSpec) DeepCopyInto(out *ComponentsSpec) {
	*out = *in
	out.Oauth = in.Oauth
	in.Server.DeepCopyInto(&out.Server)
	out.Meta = in.Meta
	in.Database.DeepCopyInto(&out.Database)
	out.Prometheus = in.Prometheus
	out.Grafana = in.Grafana
	out.Upgrade = in.Upgrade
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ComponentsSpec.
func (in *ComponentsSpec) DeepCopy() *ComponentsSpec {
	if in == nil {
		return nil
	}
	out := new(ComponentsSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *DatabaseConfiguration) DeepCopyInto(out *DatabaseConfiguration) {
	*out = *in
	in.Resources.DeepCopyInto(&out.Resources)
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new DatabaseConfiguration.
func (in *DatabaseConfiguration) DeepCopy() *DatabaseConfiguration {
	if in == nil {
		return nil
	}
	out := new(DatabaseConfiguration)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *DvConfiguration) DeepCopyInto(out *DvConfiguration) {
	*out = *in
	out.Resources = in.Resources
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new DvConfiguration.
func (in *DvConfiguration) DeepCopy() *DvConfiguration {
	if in == nil {
		return nil
	}
	out := new(DvConfiguration)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *GrafanaConfiguration) DeepCopyInto(out *GrafanaConfiguration) {
	*out = *in
	out.Resources = in.Resources
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new GrafanaConfiguration.
func (in *GrafanaConfiguration) DeepCopy() *GrafanaConfiguration {
	if in == nil {
		return nil
	}
	out := new(GrafanaConfiguration)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *JaegerConfiguration) DeepCopyInto(out *JaegerConfiguration) {
	*out = *in
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new JaegerConfiguration.
func (in *JaegerConfiguration) DeepCopy() *JaegerConfiguration {
	if in == nil {
		return nil
	}
	out := new(JaegerConfiguration)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MetaConfiguration) DeepCopyInto(out *MetaConfiguration) {
	*out = *in
	out.Resources = in.Resources
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MetaConfiguration.
func (in *MetaConfiguration) DeepCopy() *MetaConfiguration {
	if in == nil {
		return nil
	}
	out := new(MetaConfiguration)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *OauthConfiguration) DeepCopyInto(out *OauthConfiguration) {
	*out = *in
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new OauthConfiguration.
func (in *OauthConfiguration) DeepCopy() *OauthConfiguration {
	if in == nil {
		return nil
	}
	out := new(OauthConfiguration)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *PrometheusConfiguration) DeepCopyInto(out *PrometheusConfiguration) {
	*out = *in
	out.Resources = in.Resources
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new PrometheusConfiguration.
func (in *PrometheusConfiguration) DeepCopy() *PrometheusConfiguration {
	if in == nil {
		return nil
	}
	out := new(PrometheusConfiguration)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *PublicApiConfiguration) DeepCopyInto(out *PublicApiConfiguration) {
	*out = *in
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new PublicApiConfiguration.
func (in *PublicApiConfiguration) DeepCopy() *PublicApiConfiguration {
	if in == nil {
		return nil
	}
	out := new(PublicApiConfiguration)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *Resources) DeepCopyInto(out *Resources) {
	*out = *in
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new Resources.
func (in *Resources) DeepCopy() *Resources {
	if in == nil {
		return nil
	}
	out := new(Resources)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ResourcesWithPersistentVolume) DeepCopyInto(out *ResourcesWithPersistentVolume) {
	*out = *in
	if in.VolumeLabels != nil {
		in, out := &in.VolumeLabels, &out.VolumeLabels
		*out = make(map[string]string, len(*in))
		for key, val := range *in {
			(*out)[key] = val
		}
	}
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ResourcesWithPersistentVolume.
func (in *ResourcesWithPersistentVolume) DeepCopy() *ResourcesWithPersistentVolume {
	if in == nil {
		return nil
	}
	out := new(ResourcesWithPersistentVolume)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ResourcesWithVolume) DeepCopyInto(out *ResourcesWithVolume) {
	*out = *in
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ResourcesWithVolume.
func (in *ResourcesWithVolume) DeepCopy() *ResourcesWithVolume {
	if in == nil {
		return nil
	}
	out := new(ResourcesWithVolume)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ServerConfiguration) DeepCopyInto(out *ServerConfiguration) {
	*out = *in
	out.Resources = in.Resources
	in.Features.DeepCopyInto(&out.Features)
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ServerConfiguration.
func (in *ServerConfiguration) DeepCopy() *ServerConfiguration {
	if in == nil {
		return nil
	}
	out := new(ServerConfiguration)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ServerFeatures) DeepCopyInto(out *ServerFeatures) {
	*out = *in
	if in.MavenRepositories != nil {
		in, out := &in.MavenRepositories, &out.MavenRepositories
		*out = make(map[string]string, len(*in))
		for key, val := range *in {
			(*out)[key] = val
		}
	}
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ServerFeatures.
func (in *ServerFeatures) DeepCopy() *ServerFeatures {
	if in == nil {
		return nil
	}
	out := new(ServerFeatures)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *Syndesis) DeepCopyInto(out *Syndesis) {
	*out = *in
	out.TypeMeta = in.TypeMeta
	in.ObjectMeta.DeepCopyInto(&out.ObjectMeta)
	in.Spec.DeepCopyInto(&out.Spec)
	in.Status.DeepCopyInto(&out.Status)
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new Syndesis.
func (in *Syndesis) DeepCopy() *Syndesis {
	if in == nil {
		return nil
	}
	out := new(Syndesis)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyObject is an autogenerated deepcopy function, copying the receiver, creating a new runtime.Object.
func (in *Syndesis) DeepCopyObject() runtime.Object {
	if c := in.DeepCopy(); c != nil {
		return c
	}
	return nil
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *SyndesisList) DeepCopyInto(out *SyndesisList) {
	*out = *in
	out.TypeMeta = in.TypeMeta
	out.ListMeta = in.ListMeta
	if in.Items != nil {
		in, out := &in.Items, &out.Items
		*out = make([]Syndesis, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new SyndesisList.
func (in *SyndesisList) DeepCopy() *SyndesisList {
	if in == nil {
		return nil
	}
	out := new(SyndesisList)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyObject is an autogenerated deepcopy function, copying the receiver, creating a new runtime.Object.
func (in *SyndesisList) DeepCopyObject() runtime.Object {
	if c := in.DeepCopy(); c != nil {
		return c
	}
	return nil
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *SyndesisSpec) DeepCopyInto(out *SyndesisSpec) {
	*out = *in
	out.Backup = in.Backup
	in.Components.DeepCopyInto(&out.Components)
	out.Addons = in.Addons
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new SyndesisSpec.
func (in *SyndesisSpec) DeepCopy() *SyndesisSpec {
	if in == nil {
		return nil
	}
	out := new(SyndesisSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *SyndesisStatus) DeepCopyInto(out *SyndesisStatus) {
	*out = *in
	if in.LastUpgradeFailure != nil {
		in, out := &in.LastUpgradeFailure, &out.LastUpgradeFailure
		*out = (*in).DeepCopy()
	}
	out.Backup = in.Backup
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new SyndesisStatus.
func (in *SyndesisStatus) DeepCopy() *SyndesisStatus {
	if in == nil {
		return nil
	}
	out := new(SyndesisStatus)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *UpgradeConfiguration) DeepCopyInto(out *UpgradeConfiguration) {
	*out = *in
	out.Resources = in.Resources
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new UpgradeConfiguration.
func (in *UpgradeConfiguration) DeepCopy() *UpgradeConfiguration {
	if in == nil {
		return nil
	}
	out := new(UpgradeConfiguration)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *VolumeOnlyResources) DeepCopyInto(out *VolumeOnlyResources) {
	*out = *in
	return
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new VolumeOnlyResources.
func (in *VolumeOnlyResources) DeepCopy() *VolumeOnlyResources {
	if in == nil {
		return nil
	}
	out := new(VolumeOnlyResources)
	in.DeepCopyInto(out)
	return out
}
