import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { DynamicFormsBootstrapUIModule } from '@ng2-dynamic-forms/ui-bootstrap';
import { ModalModule } from 'ng2-bootstrap/modal';
import { TabsModule } from 'ng2-bootstrap';
import { ToasterModule } from 'angular2-toaster';

import { IntegrationsEditPage, editIntegrationChildRoutes } from './edit-page/edit-page.component';
import { IntegrationsConfigureActionComponent } from './edit-page/configure-action/configure-action.component';
import { IntegrationsSaveOrAddStepComponent } from './edit-page/save-or-add-step/save-or-add-step.component';
import { IntegrationsSelectActionComponent } from './edit-page/select-action/select-action.component';
import { IntegrationsSelectConnectionComponent } from './edit-page/select-connection/select-connection.component';
import { ListActionsComponent } from './edit-page/list-actions/list-actions.component';
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
    IntegrationsConfigureActionComponent,
    IntegrationsConfigureConnectionComponent,
    IntegrationsEditPage,
    IntegrationsFilterPipe,
    IntegrationsListComponent,
    IntegrationsListPage,
    IntegrationsListToolbarComponent,
    IntegrationsSaveOrAddStepComponent,
    IntegrationsSelectActionComponent,
    IntegrationsSelectConnectionComponent,
    FlowViewComponent,
    FlowViewStepComponent,
    ListActionsComponent,
  ],
  providers: [
    CurrentFlow,
  ],
})
export class IntegrationsModule {
}
