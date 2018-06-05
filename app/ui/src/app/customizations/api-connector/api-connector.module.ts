import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';
import { ApiModule } from '@syndesis/ui/api';

import { apiConnectorEndpoints } from './api-connector.api';
import { ApiConnectorRoutingModule } from './api-connector.routing';

import {
  ApiConnectorAuthComponent,
  ApiConnectorSwaggerUploadComponent
} from './api-connector-create';
import { ApiConnectorInfoComponent } from './api-connector-info';
import { ApiConnectorReviewComponent } from './api-connector-review';
import { ApiConnectorListComponent } from './api-connector-list';

import { apiConnectorReducer } from './api-connector.reducer';
import { ApiConnectorEffects } from './api-connector.effects';
import { ApiConnectorService } from './api-connector.service';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    VendorModule,
    PatternflyUIModule,
    SyndesisCommonModule,
    ApiConnectorRoutingModule,
    ApiModule.forChild(apiConnectorEndpoints),
    StoreModule.forFeature('apiConnectorState', apiConnectorReducer),
    EffectsModule.forFeature([ApiConnectorEffects])
  ],
  exports: [RouterModule],
  declarations: [
    ...ApiConnectorRoutingModule.routedComponents,
    ApiConnectorListComponent,
    ApiConnectorAuthComponent,
    ApiConnectorSwaggerUploadComponent,
    ApiConnectorInfoComponent,
    ApiConnectorReviewComponent
  ],
  providers: [ApiConnectorService]
})
export class ApiConnectorModule {}
