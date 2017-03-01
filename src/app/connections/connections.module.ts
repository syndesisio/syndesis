import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { IPaaSCommonModule } from '../common/common.module';
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

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    IPaaSCommonModule,
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
})
export class ConnectionsModule { }
