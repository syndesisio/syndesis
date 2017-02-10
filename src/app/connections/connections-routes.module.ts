import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ConnectionsModule } from './connections.module';
import { ConnectionsListPage } from './list-page/list-page.component';
import { ConnectionViewPage } from './view-page/view-page.component';

const routes: Routes = [
  { path: '', component: ConnectionsListPage, pathMatch: 'full' },
  { path: ':id', component: ConnectionViewPage, pathMatch: 'full' },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    ConnectionsModule,
  ],
  exports: [
    RouterModule,
  ],
})
export class ConnectionsRoutesModule { }


