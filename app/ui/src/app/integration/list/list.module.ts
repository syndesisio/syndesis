import { NgModule, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { IntegrationStatusComponent } from '@syndesis/ui/integration/list/status.component';
import { IntegrationStatusDetailComponent } from '@syndesis/ui/integration/list/status-detail.component';
import { IntegrationActionMenuComponent } from '@syndesis/ui/integration/list/action-menu.component';
import { IntegrationListComponent } from '@syndesis/ui/integration/list/list.component';

const syndesisCommonModuleFwd = forwardRef(() => SyndesisCommonModule);
const integrationSupportModuleFwd = forwardRef(() => IntegrationSupportModule);

@NgModule({
  imports: [
    CommonModule,
    VendorModule,
    syndesisCommonModuleFwd,
    integrationSupportModuleFwd
  ],
  declarations: [
    IntegrationActionMenuComponent,
    IntegrationStatusComponent,
    IntegrationStatusDetailComponent,
    IntegrationListComponent
  ],
  exports: [
    IntegrationActionMenuComponent,
    IntegrationListComponent,
    IntegrationStatusComponent,
    IntegrationStatusDetailComponent
  ]
})
export class IntegrationListModule {}
