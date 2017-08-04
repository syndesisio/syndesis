import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TagInputModule } from 'ngx-chips';
import { ToolbarModule } from 'patternfly-ng';

import { SyndesisCommonModule } from '../common/common.module';
import { DynamicFormsPatternflyUIModule } from '../common/ui-patternfly/ui-patternfly.module';
import { ConnectionsCreatePage } from './create-page/create-page.component';
import { ConnectionsConnectionBasicsComponent } from './create-page/connection-basics/connection-basics.component';
import { ConnectionsConfigureFieldsComponent } from './create-page/configure-fields/configure-fields.component';
import { ConnectionsReviewComponent } from './create-page/review/review.component';
import { ConnectionsCancelComponent } from './create-page/cancel.component';
import { ConnectionsListPage } from './list-page/list-page.component';
import { ConnectionsListToolbarComponent } from './list-toolbar/list-toolbar.component';
import { ConnectionsListComponent } from './list/list.component';
import { ConnectionViewPage } from './view-page/view-page.component';
import { ConnectionViewWrapperComponent } from './view-wrapper/view-wrapper.component';
import { ConnectionViewToolbarComponent } from './view-toolbar/view-toolbar.component';
import { ConnectionViewComponent } from './view/view.component';
import { CurrentConnectionService } from './create-page/current-connection';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DynamicFormsCoreModule,
    DynamicFormsPatternflyUIModule,
    RouterModule,
    SyndesisCommonModule,
    ModalModule,
    BsDropdownModule,
    TagInputModule,
    ToolbarModule,
  ],
  declarations: [
    ConnectionsCreatePage,
    ConnectionsConnectionBasicsComponent,
    ConnectionsConfigureFieldsComponent,
    ConnectionsReviewComponent,
    ConnectionsCancelComponent,
    ConnectionsListPage,
    ConnectionsListToolbarComponent,
    ConnectionsListComponent,
    ConnectionViewPage,
    ConnectionViewWrapperComponent,
    ConnectionViewToolbarComponent,
    ConnectionViewComponent,
  ],
  exports: [
    ConnectionsListToolbarComponent,
    ConnectionsListComponent,
    ConnectionViewToolbarComponent,
    ConnectionViewComponent,
  ],
  providers: [CurrentConnectionService],
})
export class ConnectionsModule {}
