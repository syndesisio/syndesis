import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { DynamicFormsBootstrapUIModule } from '@ng2-dynamic-forms/ui-bootstrap';
import { ModalModule } from 'ng2-bootstrap/modal';
import { ToasterModule } from 'angular2-toaster';
import { DropdownModule } from 'ng2-bootstrap/dropdown';

import { SyndesisCommonModule } from '../common/common.module';
import { ConnectionsCreatePage } from './create-page/create-page.component';
import { ConnectionsConnectionBasicsComponent } from './create-page/connection-basics/connection-basics.component';
import { ConnectionsConfigureFieldsComponent } from './create-page/configure-fields/configure-fields.component';
import { ConnectionsReviewComponent } from './create-page/review/review.component';
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
    DynamicFormsBootstrapUIModule,
    RouterModule,
    SyndesisCommonModule,
    ToasterModule,
    ModalModule,
    DropdownModule,
  ],
  declarations: [
    ConnectionsCreatePage,
    ConnectionsConnectionBasicsComponent,
    ConnectionsConfigureFieldsComponent,
    ConnectionsReviewComponent,
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
  providers: [
    CurrentConnectionService,
  ],
})
export class ConnectionsModule { }
