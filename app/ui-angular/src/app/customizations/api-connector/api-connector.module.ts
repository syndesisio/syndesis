import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule, PatternflyUIModule, OpenApiModule } from '@syndesis/ui/common';
import { ApiModule } from '@syndesis/ui/api';

import { apiConnectorEndpoints } from '@syndesis/ui/customizations/api-connector/api-connector.api';
import { ApiConnectorRoutingModule } from '@syndesis/ui/customizations/api-connector/api-connector.routing';

import {
  ApiConnectorAuthComponent,
  ApiConnectorSwaggerUploadComponent
} from '@syndesis/ui/customizations/api-connector/api-connector-create';
import { ApiConnectorInfoComponent } from '@syndesis/ui/customizations/api-connector/api-connector-info';
import { ApiConnectorReviewComponent } from '@syndesis/ui/customizations/api-connector/api-connector-review';
import { ApiConnectorListComponent } from '@syndesis/ui/customizations/api-connector/api-connector-list';

import { apiConnectorReducer } from '@syndesis/ui/customizations/api-connector/api-connector.reducer';
import { ApiConnectorEffects } from '@syndesis/ui/customizations/api-connector/api-connector.effects';
import { ApiConnectorService } from '@syndesis/ui/customizations/api-connector/api-connector.service';

import { ApicurioEditorModule, ApicurioCommonComponentsModule } from 'apicurio-design-studio';
import { ApiConnectorEditorComponent } from '@syndesis/ui/customizations/api-connector/api-connector-create/api-connector-editor';

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
    EffectsModule.forFeature([ApiConnectorEffects]),
    ApicurioEditorModule,
    ApicurioCommonComponentsModule,
    OpenApiModule
  ],
  exports: [RouterModule],
  declarations: [
    ...ApiConnectorRoutingModule.routedComponents,
    ApiConnectorListComponent,
    ApiConnectorAuthComponent,
    ApiConnectorSwaggerUploadComponent,
    ApiConnectorInfoComponent,
    ApiConnectorReviewComponent,
    ApiConnectorEditorComponent
  ],
  providers: [ApiConnectorService]
})
export class ApiConnectorModule {}
