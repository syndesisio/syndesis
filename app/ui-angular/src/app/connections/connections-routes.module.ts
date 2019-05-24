import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CanDeactivateGuard } from '@syndesis/ui/platform';

import { ConnectionsModule } from '@syndesis/ui/connections/connections.module';
import { ConnectionsListPage } from '@syndesis/ui/connections/list-page/list-page.component';
import { ConnectionsCreatePage } from '@syndesis/ui/connections/create-page/create-page.component';
import { ConnectionsConnectionBasicsComponent } from '@syndesis/ui/connections/create-page/connection-basics/connection-basics.component';
import { ConnectionsConfigureFieldsComponent } from '@syndesis/ui/connections/create-page/configure-fields/configure-fields.component';
import { ConnectionsReviewComponent } from '@syndesis/ui/connections/create-page/review/review.component';
import { ConnectionsCancelComponent } from '@syndesis/ui/connections/create-page/cancel.component';
import { ConnectionDetailPageComponent } from '@syndesis/ui/connections/detail-page/detail-page.component';

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
