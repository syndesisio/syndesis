import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ToolbarModule } from 'patternfly-ng';
import { SyndesisCommonModule } from '../common/common.module';
import { PatternflyUIModule } from '../common/ui-patternfly/ui-patternfly.module';

import { CustomizationsComponent } from './customizations.component';
import { ApiConnectorListComponent } from './api-connectors/api-connector-list.component';

const routes: Routes = [
  {
    path: '',
    component: CustomizationsComponent,
    children: [
      {
        path: 'api-connectors',
        component: ApiConnectorListComponent
      },
      {
        path: '',
        redirectTo: 'api-connectors'
      }
    ]
  }
];

@NgModule({
  imports: [
    CommonModule,
    PatternflyUIModule,
    ToolbarModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule
  ],
  exports: [],
  declarations: [
    CustomizationsComponent,
    ApiConnectorListComponent
  ],
  providers: []
})
export class CustomizationsModule {}
