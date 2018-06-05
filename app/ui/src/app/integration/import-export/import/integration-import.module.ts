import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { FileUploadModule } from 'ng2-file-upload';

import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';

import { IntegrationImportComponent } from './integration-import.component';
import { IntegrationImportEffects } from './integration-import.effects';
import { integrationImportReducer } from './integration-import.reducer';
import { IntegrationImportService } from './integration-import.service';

@NgModule({
  imports: [
    CommonModule,
    PatternflyUIModule,
    SyndesisCommonModule,
    StoreModule.forFeature('integrationImportState', integrationImportReducer),
    EffectsModule.forFeature([IntegrationImportEffects]),
    FileUploadModule
  ],
  exports: [RouterModule],
  declarations: [IntegrationImportComponent],
  providers: [IntegrationImportService]
})
export class IntegrationImportModule {}
