/*
	Copyright (C) 2017 Red Hat, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	        http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

import { Component, ViewChild } from '@angular/core';

import { DocumentDefinition } from 'ipaas.data.mapper';
import { MappingDefinition } from 'ipaas.data.mapper';
import { ConfigModel } from 'ipaas.data.mapper';
import { MappingModel } from 'ipaas.data.mapper';

import { ErrorHandlerService } from 'ipaas.data.mapper';
import { DocumentManagementService } from 'ipaas.data.mapper';
import { MappingManagementService } from 'ipaas.data.mapper';

import { DataMapperAppComponent } from 'ipaas.data.mapper';

@Component({
	selector: 'ipaas-data-mapper-host',
	template: `
  	<data-mapper #dataMapperComponent [cfg]="cfg"></data-mapper>
  `,
	providers: [MappingManagementService, ErrorHandlerService, DocumentManagementService],
})

export class DataMapperHostComponent {

	@ViewChild('dataMapperComponent')
	private dataMapperComponent: DataMapperAppComponent;

	public cfg: ConfigModel;

	constructor(
		private documentService: DocumentManagementService,
		private mappingService: MappingManagementService,
		private errorService: ErrorHandlerService,
		) {

		// initialize config information before initializing services
		const c: ConfigModel = new ConfigModel();
		c.baseJavaServiceUrl = 'https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/java/';
		c.baseMappingServiceUrl = 'https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/';
		c.mappingInputJavaClass = 'twitter4j.Status';
		c.mappingOutputJavaClass = 'org.apache.camel.salesforce.dto.Contact';
		c.mappings = new MappingDefinition();
		c.documentService = documentService;
		c.mappingService = mappingService;
		c.errorService = errorService;
		this.cfg = c;

		// point services' config pointers to our config
		c.documentService.cfg = c;
		c.mappingService.cfg = c;

		// fetch the input / output documents from the inspection service
		c.documentService.initialize();

		// fetch mappings from the mapping service
		// (currently hard coded to look up and use first mapping config prefixed with "UI")
		c.mappingService.initialize();

		//save the mappings when the ui calls us back asking for save
		c.mappingService.saveMappingOutput$.subscribe((saveHandler: Function) => {
			console.log('Host component saving mappings.');
			c.mappingService.saveMappingToService(saveHandler);
		});
	}
}
