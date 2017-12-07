import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ToolbarModule, ListModule } from 'patternfly-ng';
import { SyndesisCommonModule } from '../common/common.module';
import { PatternflyUIModule } from '../common/ui-patternfly/ui-patternfly.module';
import { FileUploadModule } from 'ng2-file-upload-base/src';

import { CustomizationsComponent } from './customizations.component';

import {
  ApiConnectorListComponent,
  ApiConnectorCreateComponent,
  ApiConnectorDetailComponent,
  ApiConnectorSwaggerUploadComponent,
  ApiConnectorSwaggerReviewComponent
} from './api-connector';

import {
  TechExtensionsListComponent,
  TechExtensionImportComponent,
  TechExtensionDetailComponent,
  TechExtensionDeleteModalComponent
} from './tech-extensions';

const routes: Routes = [
  {
    path: '',
    component: CustomizationsComponent,
    children: [
      {
        path: 'tech-extensions',
        component: TechExtensionsListComponent
      },
      {
        path: 'api-connector',
        component: ApiConnectorListComponent
      },
      {
        path: '',
        redirectTo: 'api-connector'
      }
    ]
  },
  {
    path: 'tech-extensions/import/:id',
    component: TechExtensionImportComponent
  },
  {
    path: 'tech-extensions/import',
    component: TechExtensionImportComponent
  },
  {
    path: 'tech-extensions/:id',
    component: TechExtensionDetailComponent
  },
  {
    path: 'api-connector/create/:template',
    component: ApiConnectorCreateComponent
  },
  {
    path: 'api-connector/create/:template/:step',
    component: ApiConnectorCreateComponent
  },
  {
    path: 'api-connector/:id',
    component: ApiConnectorDetailComponent
  }
];

@NgModule({
  imports: [
    CommonModule,
    PatternflyUIModule,
    ToolbarModule,
    ListModule,
    RouterModule.forChild(routes),
    FileUploadModule,
    SyndesisCommonModule
  ],
  exports: [],
  declarations: [
    CustomizationsComponent,
    ApiConnectorListComponent,
    ApiConnectorCreateComponent,
    ApiConnectorDetailComponent,
    ApiConnectorSwaggerUploadComponent,
    ApiConnectorSwaggerReviewComponent,

    TechExtensionsListComponent,
    TechExtensionImportComponent,
    TechExtensionDeleteModalComponent,
    TechExtensionDetailComponent
  ],
  schemas: [NO_ERRORS_SCHEMA]
})
export class CustomizationsModule { }
