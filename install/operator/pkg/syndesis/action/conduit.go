/*
 * Copyright (C) 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package action

import (
	osv1 "github.com/openshift/api/route/v1"
	k8v1 "k8s.io/api/networking/v1beta1"
	meta "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
)

//
// Interface for interrogating route and ingress objects
// avoiding lots of if/else checking logic
//
type Conduit interface {
	Target() runtime.Object
	Meta() *meta.ObjectMeta
	ExtractApplicationUrl() string
	Host() string
}

func ConduitWithName(resource runtime.Object, name string) (Conduit, bool) {
	if route, ok := resource.(*k8v1.Ingress); ok {
		if route.Name == name {
			return IngressConduit{ingress: route}, true
		}
	} else if route, ok := resource.(*osv1.Route); ok {
		if route.Name == name {
			return RouteConduit{route: route}, true
		}
	}

	return nil, false
}

//
// Route implementation
//
type RouteConduit struct {
	route *osv1.Route
}

func (ra RouteConduit) Target() runtime.Object {
	return ra.route
}

func (ra RouteConduit) Meta() *meta.ObjectMeta {
	return &ra.route.ObjectMeta
}

func (ra RouteConduit) ExtractApplicationUrl() string {
	scheme := "http"
	if ra.route.Spec.TLS != nil {
		scheme = "https"
	}
	return scheme + "://" + ra.route.Spec.Host
}

func (ra RouteConduit) Host() string {
	return ra.route.Spec.Host
}

//
// Ingress implementation
//
type IngressConduit struct {
	ingress *k8v1.Ingress
}

func (ia IngressConduit) Target() runtime.Object {
	return ia.ingress
}

func (ia IngressConduit) Meta() *meta.ObjectMeta {
	return &ia.ingress.ObjectMeta
}

func (ia IngressConduit) ExtractApplicationUrl() string {
	scheme := "http"
	if ia.ingress.Spec.TLS != nil {
		scheme = "https"
	}
	return scheme + "://" + ia.ingress.Spec.Rules[0].Host
}

func (ia IngressConduit) Host() string {
	return ia.ingress.Spec.Rules[0].Host
}
