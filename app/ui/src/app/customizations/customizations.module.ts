import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FileUploadModule } from 'ng2-file-upload';
import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';

import {
  ApiConnectorListComponent,
  ApiConnectorLazyLoaderGuard
} from './api-connector';
import { ApiConnectorModule } from './api-connector/api-connector.module';
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
        path: 'extensions',
        component: TechExtensionsListComponent
      },
      {
        path: 'api-connector',
        component: ApiConnectorListComponent,
        canActivate: [ApiConnectorLazyLoaderGuard]
      },
      {
        path: '',
        redirectTo: 'api-connector'
      }
    ]
    // TODO: Move to its own NgRoutingModule
  },
  {
    path: 'extensions/import/:id',
    component: TechExtensionImportComponent
  },
  {
    path: 'extensions/import',
    component: TechExtensionImportComponent
  },
  {
    path: 'extensions/:id',
    component: TechExtensionDetailComponent
  }
];

@NgModule({
  imports: [
    CommonModule,
    SyndesisCommonModule,
    VendorModule,
    PatternflyUIModule,
    FileUploadModule,
    ApiConnectorModule,
    RouterModule.forChild(routes)
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
export class CustomizationsModule {}
