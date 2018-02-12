import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { IntegrationImportComponent } from './integration-import.component';

const routes: Routes = [{
  path: 'integration-import',
  component: IntegrationImportComponent
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class IntegrationImportRoutingModule {
  static routedComponents = [
    IntegrationImportComponent
  ];
}
