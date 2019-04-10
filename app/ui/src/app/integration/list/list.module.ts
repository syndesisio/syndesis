import { NgModule, forwardRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { IntegrationStatusComponent } from '@syndesis/ui/integration/list/status.component';
import { IntegrationStatusDetailComponent } from '@syndesis/ui/integration/list/status-detail.component';
import { IntegrationActionMenuComponent } from '@syndesis/ui/integration/list/action-menu.component';
import { IntegrationListComponent } from '@syndesis/ui/integration/list/list.component';
import { TagCICDModalComponent } from './tag-cicd-modal.component';

const syndesisCommonModuleFwd = forwardRef(() => SyndesisCommonModule);
const integrationSupportModuleFwd = forwardRef(() => IntegrationSupportModule);

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    VendorModule,
    FormsModule,
    syndesisCommonModuleFwd,
    integrationSupportModuleFwd,
  ],
  declarations: [
    IntegrationActionMenuComponent,
    IntegrationStatusComponent,
    IntegrationStatusDetailComponent,
    IntegrationListComponent,
    TagCICDModalComponent,
  ],
  exports: [
    IntegrationActionMenuComponent,
    IntegrationListComponent,
    IntegrationStatusComponent,
    IntegrationStatusDetailComponent,
    TagCICDModalComponent,
  ],
})
export class IntegrationListModule {}
