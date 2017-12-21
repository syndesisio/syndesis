import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FileUploadModule } from 'ng2-file-upload-base/src';
import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';

import { ApiConnectorModule, ApiConnectorListComponent } from './api-connector';
import { CustomizationsComponent } from './customizations.component';

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
  // TODO: Move to its own NgRoutingModule
  }, {
    path: 'tech-extensions/import/:id',
    component: TechExtensionImportComponent
  }, {
    path: 'tech-extensions/import',
    component: TechExtensionImportComponent
  }, {
    path: 'tech-extensions/:id',
    component: TechExtensionDetailComponent
  }
];

@NgModule({
  imports: [
    CommonModule,
    SyndesisCommonModule,
    PatternflyUIModule,
    FileUploadModule,
    RouterModule.forChild(routes),
    ApiConnectorModule,
  ],
  exports: [RouterModule],
  declarations: [
    CustomizationsComponent,
    // TODO: Move this out into its own NgModule
    TechExtensionsListComponent,
    TechExtensionImportComponent,
    TechExtensionDeleteModalComponent,
    TechExtensionDetailComponent
  ]
})
export class CustomizationsModule { }
