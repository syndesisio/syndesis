import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';

import { IntegrationImportComponent } from './integration-import.component';

import { IntegrationImportEffects } from './integration-import.effects';
import { integrationImportReducer } from './integration-import.reducer';
import { IntegrationImportService } from './integration-import.service';

const routes: Routes = [{
  path: 'import-export/import',
  component: IntegrationImportComponent
}];

@NgModule({
  imports: [
    CommonModule,
    PatternflyUIModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule,
    StoreModule.forFeature('importState', integrationImportReducer),
    EffectsModule.forFeature([IntegrationImportEffects]),
  ],
  exports: [RouterModule],
  declarations: [
    IntegrationImportComponent,
  ],
  providers: [
    IntegrationImportService
  ]
})
export class IntegrationImportModule { }
