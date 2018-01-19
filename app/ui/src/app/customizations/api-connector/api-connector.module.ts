import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

import { CoreModule } from '@syndesis/ui/core';
import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';
import { API_ENDPOINTS, ApiConfigService } from '@syndesis/ui/platform';

import {
  ApiConnectorAuthComponent,
  ApiConnectorSwaggerUploadComponent,
  ApiConnectorCreateComponent,
} from './api-connector-create';
import { ApiConnectorInfoComponent } from './api-connector-info';
import { ApiConnectorReviewComponent } from './api-connector-review';
import { ApiConnectorListComponent } from './api-connector-list';
import { ApiConnectorDetailComponent } from './api-connector-detail';

import { ApiConnectorEffects } from './api-connector.effects';
import { apiConnectorReducer } from './api-connector.reducer';
import { ApiConnectorLazyLoaderGuard } from './api-connector-lazy-loader.guard';
import { ApiConnectorService } from './api-connector.service';
import { apiConnectorEndpoints } from './api-connector.api';

const routes: Routes = [{
  path: 'api-connector/create/:template',
  component: ApiConnectorCreateComponent
}, {
  path: 'api-connector/create/:template/:step',
  component: ApiConnectorCreateComponent
}, {
  path: 'api-connector/:id',
  component: ApiConnectorDetailComponent,
  canActivate: [ApiConnectorLazyLoaderGuard]
}];

@NgModule({
  imports: [
    CommonModule,
    PatternflyUIModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule,
    StoreModule.forFeature('apiConnectorState', apiConnectorReducer),
    EffectsModule.forFeature([ApiConnectorEffects]),
    CoreModule.forFeature({ apiConfig: apiConnectorEndpoints }),
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
    ApiConnectorLazyLoaderGuard
  ]
})
export class ApiConnectorModule { }
