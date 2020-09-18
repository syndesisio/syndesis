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

package main

import (
	"fmt"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/capabilities"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
)

func main() {

	errMsg := "Error: Detection of cluster failed. "

	ct := &clienttools.ClientTools{}
	apiSpec, err := capabilities.ApiCapabilities(ct)
	if err != nil {
		fmt.Println(errMsg, err)
	} else {
		fmt.Println("Version:", apiSpec.Version)
		fmt.Println("Imagestreams:", apiSpec.ImageStreams)
		fmt.Println("Routes:", apiSpec.Routes)
		fmt.Println("AuthProvider:", apiSpec.EmbeddedProvider)
		fmt.Println("ConsoleLink:", apiSpec.ConsoleLink)
	}
}
