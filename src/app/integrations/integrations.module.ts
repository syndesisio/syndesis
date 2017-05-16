import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { DynamicFormsBootstrapUIModule } from '@ng2-dynamic-forms/ui-bootstrap';
import { CollapseModule, ModalModule, PopoverModule, TabsModule, BsDropdownModule } from 'ngx-bootstrap';
import { ToasterModule } from 'angular2-toaster';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { DataMapperModule } from 'syndesis.data.mapper';

import { IntegrationsEditPage } from './edit-page/edit-page.component';
import { IntegrationBasicsComponent } from './edit-page/integration-basics/integration-basics.component';
import { IntegrationsSelectConnectionComponent } from './edit-page/connection-select/connection-select.component';
import { IntegrationsConfigureActionComponent } from './edit-page/action-configure/action-configure.component';
import { IntegrationsSelectActionComponent } from './edit-page/action-select/action-select.component';
import { IntegrationsSaveOrAddStepComponent } from './edit-page/save-or-add-step/save-or-add-step.component';
import { IntegrationsStepSelectComponent } from './edit-page/step-select/step-select.component';
import { IntegrationsStepConfigureComponent } from './edit-page/step-configure/step-configure.component';
import { DataMapperHostComponent } from './edit-page/step-configure/data-mapper-host.component';
import { ListActionsComponent } from './edit-page/list-actions/list-actions.component';

import { IntegrationsListPage } from './list-page/list-page.component';
import { IntegrationsListToolbarComponent } from './list-toolbar/list-toolbar.component';
import { IntegrationsFilterPipe } from './integrations-filter.pipe';
import { IntegrationsListComponent } from './list/list.component';
import { FlowViewComponent } from './edit-page/flow-view/flow-view.component';
import { FlowViewStepComponent } from './edit-page/flow-view/flow-view-step.component';
import { CurrentFlow } from './edit-page/current-flow.service';
import { SyndesisCommonModule } from '../common/common.module';
import { ConnectionsModule } from '../connections/connections.module';

const editIntegrationChildRoutes = [
  { path: 'save-or-add-step', component: IntegrationsSaveOrAddStepComponent },
  { path: 'integration-basics', component: IntegrationBasicsComponent },
  { path: 'connection-select/:position', component: IntegrationsSelectConnectionComponent },
  { path: 'action-select/:position', component: IntegrationsSelectActionComponent },
  { path: 'action-configure/:position', component: IntegrationsConfigureActionComponent },
  { path: 'step-select/:position', component: IntegrationsStepSelectComponent },
  { path: 'step-configure/:position', component: IntegrationsStepConfigureComponent },
];

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
    SyndesisCommonModule,
    CollapseModule,
    ToasterModule,
    TooltipModule,
    ModalModule,
    PopoverModule,
    BsDropdownModule,
    DataMapperModule,
  ],
  declarations: [
    DataMapperHostComponent,
    IntegrationsConfigureActionComponent,
    IntegrationsEditPage,
    IntegrationBasicsComponent,
    IntegrationsFilterPipe,
    IntegrationsListComponent,
    IntegrationsSelectConnectionComponent,
    IntegrationsSaveOrAddStepComponent,
    IntegrationsStepSelectComponent,
    IntegrationsStepConfigureComponent,
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
