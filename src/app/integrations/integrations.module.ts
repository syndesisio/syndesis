import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { DynamicFormsBootstrapUIModule } from '@ng2-dynamic-forms/ui-bootstrap';
import { ModalModule } from 'ng2-bootstrap/modal';
import { TabsModule } from 'ng2-bootstrap';
import { ToasterModule, ToasterService } from 'angular2-toaster';

import { IntegrationsEditPage, editIntegrationChildRoutes } from './edit-page/edit-page.component';
import { IntegrationsSelectConnectionComponent } from './edit-page/select-connection/select-connection.component';
import { IntegrationsConfigureConnectionComponent } from './edit-page/configure-connection/configure-connection.component';
import { IntegrationsListPage } from './list-page/list-page.component';
import { IntegrationsListToolbarComponent } from './list-toolbar/list-toolbar.component';
import { IntegrationsFilterPipe } from './integrations-filter.pipe';
import { IntegrationsListComponent } from './list/list.component';
import { FlowViewComponent } from './edit-page/flow-view/flow-view.component';
import { FlowViewStepComponent } from './edit-page/flow-view/flow-view-step.component';
import { CurrentFlow } from './edit-page/current-flow.service';
import { IPaaSCommonModule } from '../common/common.module';
import { ConnectionsModule } from '../connections/connections.module';
import { CollapseModule } from 'ng2-bootstrap';

const routes: Routes = [
  { path: '', component: IntegrationsListPage, pathMatch: 'full' },
  {
    path: 'create',
    component: IntegrationsEditPage,
    children: editIntegrationChildRoutes,
  },
  {
    path: 'edit/:integrationId',
    component: IntegrationsEditPage,
    children: editIntegrationChildRoutes,
  },
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DynamicFormsCoreModule,
    DynamicFormsBootstrapUIModule,
    RouterModule.forChild(routes),
    ConnectionsModule,
    TabsModule,
    IPaaSCommonModule,
    CollapseModule,
    ToasterModule,
    ModalModule,
  ],
  declarations: [
    IntegrationsEditPage,
    IntegrationsSelectConnectionComponent,
    IntegrationsConfigureConnectionComponent,
    IntegrationsListPage,
    IntegrationsListToolbarComponent,
    IntegrationsListComponent,
    IntegrationsFilterPipe,
    FlowViewComponent,
    FlowViewStepComponent,
  ],
  providers: [
    CurrentFlow,
  ],
})
export class IntegrationsModule {
}
