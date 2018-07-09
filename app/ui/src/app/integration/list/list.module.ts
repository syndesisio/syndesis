import { NgModule, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { IntegrationSupportModule } from '../integration-support.module';
import { IntegrationStatusComponent } from './status.component';
import { IntegrationStatusDetailComponent } from './status-detail.component';
import { IntegrationActionMenuComponent } from './action-menu.component';
import { IntegrationListComponent } from './list.component';

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
