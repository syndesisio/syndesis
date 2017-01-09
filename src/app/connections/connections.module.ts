import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { IPaaSCommonModule } from '../common/common.module';
import { ConnectionsListPage } from './list-page/list-page.component';
import { ConnectionsListToolbarComponent } from './list-toolbar/list-toolbar.component';
import { ConnectionsListComponent } from './list/list.component';
import { ConnectionViewPage } from './view-page/view-page.component';
import { ConnectionViewWrapperComponent } from './view-wrapper/view-wrapper.component';
import { ConnectionViewToolbarComponent } from './view-toolbar/view-toolbar.component';
import { ConnectionViewComponent } from './view/view.component';

const routes: Routes = [
  { path: '', component: ConnectionsListPage, pathMatch: 'full' },
  { path: ':id', component: ConnectionViewPage, pathMatch: 'full' },
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes),
    IPaaSCommonModule,
  ],
  declarations: [
    ConnectionsListPage,
    ConnectionsListToolbarComponent,
    ConnectionsListComponent,
    ConnectionViewPage,
    ConnectionViewWrapperComponent,
    ConnectionViewToolbarComponent,
    ConnectionViewComponent,
  ],
})
export class ConnectionsModule {
}
