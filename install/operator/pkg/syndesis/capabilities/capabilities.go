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

package capabilities

import (
	"errors"

	errs "github.com/pkg/errors"
	"k8s.io/client-go/kubernetes"
)

type ApiServerSpec struct {
	Version          string // Set to the kubernetes version of the API Server
	ImageStreams     bool   // Set to true if the API Server supports imagestreams
	Routes           bool   // Set to true if the API Server supports routes
	EmbeddedProvider bool   // Set to true if the API Server support an embedded authenticaion provider, eg. openshift
}

/*
 * For testing if the giving platform is Openshift
 */
func ApiCapabilities(apiClient kubernetes.Interface) (*ApiServerSpec, error) {
	if apiClient == nil {
		return nil, errors.New("No api client. Cannot determine api capabilities")
	}

	apiSpec := ApiServerSpec{}

	info, err := apiClient.Discovery().ServerVersion()
	if err != nil {
		return nil, errs.Wrap(err, "Failed to discover server version")
	}

	apiSpec.Version = info.Major + "." + info.Minor

	apis, err := apiClient.Discovery().ServerGroups()
	if err != nil {
		return nil, err
	}

	for _, api := range apis.Groups {
		//
		// By definition an API Server supporting Openshift APIs
		// is more than likely going to be Openshift or closely
		// compatible.
		if api.Name == "route.openshift.io" {
			apiSpec.Routes = true
		} else if api.Name == "image.openshift.io" {
			apiSpec.ImageStreams = true
		} else if api.Name == "oauth.openshift.io" {
			apiSpec.EmbeddedProvider = true
		}
	}

	return &apiSpec, nil
}
