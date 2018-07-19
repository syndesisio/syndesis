import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { SupportComponent } from '@syndesis/ui/support/support.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';

import { VendorModule } from '@syndesis/ui/vendor';
import { PatternflyUIModule } from '@syndesis/ui/common/ui-patternfly/ui-patternfly.module';
import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';

const routes: Routes = [
  { path: '', component: SupportComponent, pathMatch: 'full' }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PatternflyUIModule,
    VendorModule,
    SyndesisCommonModule,
    IntegrationSupportModule,
    RouterModule.forChild(routes),
  ],
  declarations: [
    SupportComponent
  ]
})
export class SupportModule {}
