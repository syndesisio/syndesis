import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WindowRef } from '@syndesis/ui/customizations/window-ref';
import { ApiModule } from '@syndesis/ui/api';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { ApicurioCommonComponentsModule, ApicurioEditorModule } from 'apicurio-design-studio';

import {
  PatternflyUIModule,
  SyndesisCommonModule,
  OpenApiModule
} from '@syndesis/ui/common';
import { apiProviderEndpoints } from '@syndesis/ui/integration/api-provider/api-provider.api';
import { apiProviderReducer } from '@syndesis/ui/integration/api-provider/api-provider.reducer';
import { ApiProviderEffects } from '@syndesis/ui/integration/api-provider/api-provider.effects';
import { ApiProviderService } from '@syndesis/ui/integration/api-provider/api-provider.service';
import { ApiProviderOperationsComponent } from '@syndesis/ui/integration/api-provider/operations/operations.component';
import { ApiProviderSpecComponent } from '@syndesis/ui/integration/api-provider/spec/spec.component';
import { RouterModule, Routes } from '@angular/router';
import { VendorModule } from '@syndesis/ui/vendor';
import { FormsModule } from '@angular/forms';
import { StepUploadComponent } from './spec/step-upload/step-upload.component';
import { StepValidateComponent } from './spec/step-validate/step-validate.component';

const routes: Routes = [];

@NgModule({
  imports: [
    CommonModule,
    SyndesisCommonModule,
    PatternflyUIModule,
    VendorModule,
    OpenApiModule,
    FormsModule,
    ApiModule.forChild(apiProviderEndpoints),
    StoreModule.forFeature('apiProviderState', apiProviderReducer),
    EffectsModule.forFeature([ApiProviderEffects]),
    RouterModule.forChild(routes),
    ApicurioEditorModule,
    ApicurioCommonComponentsModule
  ],
  declarations: [
    ApiProviderOperationsComponent,
    ApiProviderSpecComponent,
    StepUploadComponent,
    StepValidateComponent,
  ],
  exports: [
    ApiProviderOperationsComponent,
    ApiProviderSpecComponent
  ],
  providers: [WindowRef, ApiProviderService]
})
export class ApiProviderModule { }
