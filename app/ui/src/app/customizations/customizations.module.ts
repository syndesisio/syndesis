import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FileUploadModule } from 'ng2-file-upload';
import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';
import { WindowRef } from '@syndesis/ui/customizations/window-ref';

import {
  ApiConnectorListComponent,
  ApiConnectorLazyLoaderGuard
} from '@syndesis/ui/customizations/api-connector';
import { ApiConnectorModule } from '@syndesis/ui/customizations/api-connector/api-connector.module';
import { CustomizationsComponent } from '@syndesis/ui/customizations/customizations.component';

import {
  TechExtensionsListComponent,
  TechExtensionImportComponent,
  TechExtensionDetailComponent,
  TechExtensionDeleteModalComponent
} from '@syndesis/ui/customizations/tech-extensions';

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
  ],
  providers: [WindowRef]
})
export class CustomizationsModule {}
