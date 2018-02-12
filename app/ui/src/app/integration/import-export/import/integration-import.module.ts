import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';

import { IntegrationImportComponent } from './integration-import.component';
import { IntegrationImportRoutingModule } from './integration-import.routing';
import { IntegrationImportEffects } from './integration-import.effects';
import { integrationImportReducer } from './integration-import.reducer';
import { IntegrationImportService } from './integration-import.service';

const routes: Routes = [{
  path: 'integration-import',
  component: IntegrationImportComponent
}];

@NgModule({
  imports: [
    CommonModule,
    PatternflyUIModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule,
    IntegrationImportRoutingModule,
    StoreModule.forFeature('integrationImportState', integrationImportReducer),
    EffectsModule.forFeature([IntegrationImportEffects]),
  ],
  exports: [RouterModule],
  declarations: [
    IntegrationImportComponent,
  ],
  providers: [IntegrationImportService]
})
export class IntegrationImportModule { }
