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

import { ApicurioEditorModule, ApicurioCommonComponentsModule } from 'apicurio-design-studio';

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
<<<<<<< HEAD
    EffectsModule.forFeature([ApiConnectorEffects]),
    ApicurioEditorModule,
    ApicurioCommonComponentsModule
=======
    EffectsModule.forFeature([ApiConnectorEffects])
>>>>>>> chore(deps): upgrade angular core and rxjs
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
