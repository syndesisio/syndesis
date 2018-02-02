import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { SupportComponent } from './support.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IntegrationSupportModule } from '../integration/integration-support.module';

import { PatternflyUIModule } from '../common/ui-patternfly/ui-patternfly.module';
import { SyndesisCommonModule } from '../common/common.module';

const routes: Routes = [
  { path: '', component: SupportComponent, pathMatch: 'full' }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PatternflyUIModule,
    SyndesisCommonModule,
    IntegrationSupportModule,
    RouterModule.forChild(routes),
  ],
  declarations: [
    SupportComponent
  ]
})
export class SupportModule {}
