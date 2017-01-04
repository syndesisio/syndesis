import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { EffectsModule } from '@ngrx/effects';

import { IPaaSCommonModule } from '../common/common.module';
import { ConnectionsListPage } from './list-page/list-page.component';
import { ConnectionsListToolbarComponent } from './list-toolbar/list-toolbar.component';
import { ConnectionsListComponent } from './list/list.component';
import { ConnectionEffects } from '../store/connection/connection.effects';

const routes: Routes = [
  { path: '', component: ConnectionsListPage, pathMatch: 'full' },
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes),
    EffectsModule.run(ConnectionEffects),
    IPaaSCommonModule,
  ],
  declarations: [
    ConnectionsListPage,
    ConnectionsListToolbarComponent,
    ConnectionsListComponent,
  ],
})
export class ConnectionsModule { }
