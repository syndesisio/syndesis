import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { TagInputModule } from 'ngx-chips';

import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';
import { PatternflyUIModule } from '@syndesis/ui/common/ui-patternfly/ui-patternfly.module';
import { ConnectionsCreatePage } from '@syndesis/ui/connections/create-page/create-page.component';
import { ConnectionsConnectionBasicsComponent } from '@syndesis/ui/connections/create-page/connection-basics/connection-basics.component';
import { ConnectionsConfigureFieldsComponent } from '@syndesis/ui/connections/create-page/configure-fields/configure-fields.component';
import { ConnectionsReviewComponent } from '@syndesis/ui/connections/create-page/review/review.component';
import { ConnectionsCancelComponent } from '@syndesis/ui/connections/create-page/cancel.component';
import { ConnectionsListPage } from '@syndesis/ui/connections/list-page/list-page.component';
import { ConnectionsListComponent } from '@syndesis/ui/connections/list/list.component';
import { CurrentConnectionService } from '@syndesis/ui/connections/create-page/current-connection';
import { ConnectionDetailPageComponent } from '@syndesis/ui/connections/detail-page/detail-page.component';
import { ConnectionDetailBreadcrumbComponent } from '@syndesis/ui/connections/detail-page/breadcrumb.component';
import { ConnectionDetailInfoComponent } from '@syndesis/ui/connections/detail-page/info.component';
import { ConnectionDetailConfigurationComponent } from '@syndesis/ui/connections/detail-page/configuration.component';
import { ConnectionConfigurationService } from '@syndesis/ui/connections/common/configuration/configuration.service';
import { ConnectionConfigurationValidationComponent } from '@syndesis/ui/connections/common/configuration/validation.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DynamicFormsCoreModule,
    PatternflyUIModule,
    RouterModule,
    SyndesisCommonModule,
    TagInputModule,
    VendorModule
  ],
  declarations: [
    ConnectionsCreatePage,
    ConnectionsConnectionBasicsComponent,
    ConnectionsConfigureFieldsComponent,
    ConnectionsReviewComponent,
    ConnectionsCancelComponent,
    ConnectionsListPage,
    ConnectionsListComponent,
    ConnectionDetailPageComponent,
    ConnectionDetailBreadcrumbComponent,
    ConnectionDetailInfoComponent,
    ConnectionDetailConfigurationComponent,
    ConnectionConfigurationValidationComponent
  ],
  exports: [ConnectionsListComponent],
  providers: [CurrentConnectionService, ConnectionConfigurationService]
})
export class ConnectionsModule {}
