import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ToolbarModule, ListModule } from 'patternfly-ng';
import { SyndesisCommonModule } from '../common/common.module';
import { PatternflyUIModule } from '../common/ui-patternfly/ui-patternfly.module';

import { CustomizationsComponent } from './customizations.component';

import {
  ApiConnectorListComponent,
  ApiConnectorCreateComponent
} from './api-connector';

import {  } from './api-connectors/create-api-connector.component';
import { TechExtensionsListComponent } from './tech-extensions/tech-extensions-list.component';
import { TechExtensionImportComponent } from './tech-extensions/import/tech-extension-import.component';
import { TechExtensionDetailComponent } from './tech-extensions/detail/tech-extension-detail.component';

const routes: Routes = [
  {
    path: '',
    component: CustomizationsComponent,
    children: [
      {
        path: 'tech-extensions',
        component: TechExtensionsListComponent,
      },
      {
        path: 'api-connectors',
        component: ApiConnectorListComponent
      },
      {
        path: '',
        redirectTo: 'api-connectors'
      }
    ]
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
    path: 'api-connectors/create',
    component: ApiConnectorCreateComponent
  }
];

@NgModule({
  imports: [
    CommonModule,
    PatternflyUIModule,
    ToolbarModule,
    ListModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule
  ],
  exports: [],
  declarations: [
    CustomizationsComponent,
    ApiConnectorListComponent,
    ApiConnectorCreateComponent,
    TechExtensionsListComponent,
    TechExtensionImportComponent,
    TechExtensionDetailComponent
  ],
  providers: []
})
export class CustomizationsModule {}
