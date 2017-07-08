import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ConnectionsModule } from './connections.module';
import { ConnectionsListPage } from './list-page/list-page.component';
import { ConnectionViewPage } from './view-page/view-page.component';
import { ConnectionsCreatePage } from './create-page/create-page.component';
import { ConnectionsConnectionBasicsComponent } from './create-page/connection-basics/connection-basics.component';
import { ConnectionsConfigureFieldsComponent } from './create-page/configure-fields/configure-fields.component';
import { ConnectionsReviewComponent } from './create-page/review/review.component';

const routes: Routes = [
  { path: '', component: ConnectionsListPage, pathMatch: 'full' },
  {
    path: 'create',
    component: ConnectionsCreatePage,
    children: [
      {
        path: 'connection-basics',
        component: ConnectionsConnectionBasicsComponent,
      },
      {
        path: 'configure-fields',
        component: ConnectionsConfigureFieldsComponent,
      },
      { path: 'review', component: ConnectionsReviewComponent },
      { path: '**', redirectTo: 'connection-basics', pathMatch: 'full' },
    ],
  },
  { path: ':id', component: ConnectionViewPage, pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forChild(routes), ConnectionsModule],
  exports: [RouterModule],
})
export class ConnectionsRoutesModule {}
