import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { StoreModule } from '@ngrx/store';

import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';

import {
  ApiConnectorAuthComponent,
  ApiConnectorSwaggerUploadComponent,
  ApiConnectorCreateComponent,
} from './api-connector-create';
import { ApiConnectorInfoComponent } from './api-connector-info';
import { ApiConnectorReviewComponent } from './api-connector-review';
import { ApiConnectorListComponent } from './api-connector-list.component';
import { ApiConnectorDetailComponent } from './api-connector-detail.component';

import { ApiConnectorService } from './api-connector.service';
import { apiConnectorReducer } from './api-connector.reducer';

const routes: Routes = [{
  path: 'api-connector/create/:template',
  component: ApiConnectorCreateComponent
}, {
  path: 'api-connector/create/:template/:step',
  component: ApiConnectorCreateComponent
}, {
  path: 'api-connector/:id',
  component: ApiConnectorDetailComponent
}];

@NgModule({
  imports: [
    CommonModule,
    PatternflyUIModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule,
    StoreModule.forFeature('apiConnectorState', apiConnectorReducer)
  ],
  exports: [RouterModule],
  declarations: [
    ApiConnectorListComponent,
    ApiConnectorCreateComponent,
    ApiConnectorDetailComponent,
    ApiConnectorAuthComponent,
    ApiConnectorSwaggerUploadComponent,
    ApiConnectorCreateComponent,
    ApiConnectorInfoComponent,
    ApiConnectorReviewComponent,
  ],
  providers: [
    ApiConnectorService,
  ]
})
export class ApiConnectorModule { }
