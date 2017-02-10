import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { TabsModule } from 'ng2-bootstrap';

import { IntegrationsCreatePage } from './create-page/create-page.component';
import { IntegrationsSelectConnectionComponent } from './create-page/select-connection/select-connection.component';
import { IntegrationsConfigureConnectionComponent } from './create-page/configure-connection/configure-connection.component';
import { IntegrationsListPage } from './list-page/list-page.component';
import { IntegrationsListToolbarComponent } from './list-toolbar/list-toolbar.component';
import { IntegrationsFilterPipe } from './integrations-filter.pipe';
import { IntegrationsListComponent } from './list/list.component';
import { FlowViewComponent } from './create-page/flow-view/flow-view.component';
import { IPaaSCommonModule } from '../common/common.module';
import { ConnectionsModule } from '../connections/connections.module';

const routes: Routes = [
  { path: '', component: IntegrationsListPage, pathMatch: 'full' },
];

// Set up routes for creating and editing using the same controllers
['create', 'edit/:integrationId'].forEach((route) => {
  routes.push({ 
    path: route, 
    component: IntegrationsCreatePage, 
    children: [
      { path: 'connection-select/:connectionId', component: IntegrationsSelectConnectionComponent },
      { path: 'connection-configure/:connectionId', component: IntegrationsConfigureConnectionComponent },
    ]
  });
});

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes),
    ConnectionsModule,
    TabsModule,
    IPaaSCommonModule,
  ],
  declarations: [
    IntegrationsCreatePage,
    IntegrationsSelectConnectionComponent,
    IntegrationsConfigureConnectionComponent,
    IntegrationsListPage,
    IntegrationsListToolbarComponent,
    IntegrationsListComponent,
    IntegrationsFilterPipe,
    FlowViewComponent,
  ]
})
export class IntegrationsModule {
}
