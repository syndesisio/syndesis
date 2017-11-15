import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ConnectionsModule } from './connections.module';
import { ConnectionsListPage } from './list-page/list-page.component';
import { ConnectionsCreatePage } from './create-page/create-page.component';
import { ConnectionsConnectionBasicsComponent } from './create-page/connection-basics/connection-basics.component';
import { ConnectionsConfigureFieldsComponent } from './create-page/configure-fields/configure-fields.component';
import { ConnectionsReviewComponent } from './create-page/review/review.component';
import { ConnectionsCancelComponent } from './create-page/cancel.component';
import { CanDeactivateGuard } from '../common/can-deactivate-guard.service';
import { ConnectionDetailPageComponent } from './detail-page/detail-page.component';

const routes: Routes = [
  { path: '', component: ConnectionsListPage, pathMatch: 'full' },
  {
    path: 'create',
    component: ConnectionsCreatePage,
    children: [
      {
        path: 'connection-basics',
        component: ConnectionsConnectionBasicsComponent,
        canDeactivate: [CanDeactivateGuard]
      },
      {
        path: 'configure-fields',
        component: ConnectionsConfigureFieldsComponent,
        canDeactivate: [CanDeactivateGuard]
      },
      {
        path: 'review',
        component: ConnectionsReviewComponent,
        canDeactivate: [CanDeactivateGuard]
      },
      { path: 'cancel', component: ConnectionsCancelComponent },
      { path: '**', redirectTo: 'connection-basics', pathMatch: 'full' }
    ]
  },
  { path: ':id', component: ConnectionDetailPageComponent, pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes), ConnectionsModule],
  exports: [RouterModule]
})
export class ConnectionsRoutesModule {}
