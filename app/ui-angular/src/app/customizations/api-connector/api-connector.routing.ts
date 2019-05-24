import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { ApiConnectorCreateComponent } from '@syndesis/ui/customizations/api-connector/api-connector-create';
import { ApiConnectorDetailComponent } from '@syndesis/ui/customizations/api-connector/api-connector-detail';
import { ApiConnectorLazyLoaderGuard } from '@syndesis/ui/customizations/api-connector/api-connector-lazy-loader.guard';

const routes: Routes = [
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
    component: ApiConnectorDetailComponent,
    canActivate: [ApiConnectorLazyLoaderGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [ApiConnectorLazyLoaderGuard]
})
export class ApiConnectorRoutingModule {
  static routedComponents = [
    ApiConnectorCreateComponent,
    ApiConnectorDetailComponent
  ];
}
