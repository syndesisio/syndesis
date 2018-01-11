import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import {
  CollapseModule,
  ModalModule,
  PopoverModule,
  TabsModule
} from 'ngx-bootstrap';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { DataMapperModule } from '@atlasmap/atlasmap.data.mapper';
import { ToolbarModule } from 'patternfly-ng';

import { IntegrationsListModule } from './list/list.module';
import { PatternflyUIModule } from '../common/ui-patternfly/ui-patternfly.module';
import { IntegrationsDetailComponent } from './detail-page/detail.component';
import { IntegrationsListPage } from './list-page/list-page.component';

import {
  IntegrationsEditPage,
  IntegrationBasicsComponent,
  IntegrationsSelectConnectionComponent,
  IntegrationsConfigureActionComponent,
  IntegrationsSelectActionComponent,
  IntegrationsSaveOrAddStepComponent,
  IntegrationsStepSelectComponent,
  StepVisiblePipe,
  IntegrationsStepConfigureComponent,
  DataMapperHostComponent,
  BasicFilterComponent,
  ListActionsComponent,
  CancelAddStepComponent,
  FlowViewComponent,
  FlowViewStepComponent,
  CurrentFlow
} from './edit-page';

import { SyndesisCommonModule } from '@syndesis/ui/common';
import { ConnectionsModule } from '@syndesis/ui/connections';
import { FileUploadModule } from 'ng2-file-upload';
import { TourNgxBootstrapModule } from 'ngx-tour-ngx-bootstrap';

const editIntegrationChildRoutes = [
  { path: 'save-or-add-step', component: IntegrationsSaveOrAddStepComponent },
  { path: 'integration-basics', component: IntegrationBasicsComponent },
  {
    path: 'connection-select/:position',
    component: IntegrationsSelectConnectionComponent
  },
  {
    path: 'action-select/:position',
    component: IntegrationsSelectActionComponent
  },
  {
    path: 'action-configure/:position/:page',
    component: IntegrationsConfigureActionComponent
  },
  {
    path: 'action-configure/:position',
    component: IntegrationsConfigureActionComponent
  },
  { path: 'step-select/:position', component: IntegrationsStepSelectComponent },
  {
    path: 'step-configure/:position',
    component: IntegrationsStepConfigureComponent
  }
];

const routes: Routes = [
  { path: '', component: IntegrationsListPage, pathMatch: 'full' },
  {
    path: 'create',
    component: IntegrationsEditPage,
    children: editIntegrationChildRoutes
  },
  {
    path: ':integrationId',
    component: IntegrationsDetailComponent
  },
  {
    path: ':integrationId/edit',
    component: IntegrationsEditPage,
    children: editIntegrationChildRoutes
  }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DynamicFormsCoreModule,
    IntegrationsListModule,
    PatternflyUIModule,
    RouterModule.forChild(routes),
    ConnectionsModule,
    TabsModule,
    SyndesisCommonModule,
    CollapseModule,
    TooltipModule,
    ModalModule,
    PopoverModule,
    DataMapperModule,
    FileUploadModule,
    TourNgxBootstrapModule,
    ToolbarModule
  ],
  declarations: [
    DataMapperHostComponent,
    BasicFilterComponent,
    IntegrationsConfigureActionComponent,
    IntegrationsEditPage,
    IntegrationBasicsComponent,
    IntegrationsDetailComponent,
    IntegrationsSelectConnectionComponent,
    IntegrationsSaveOrAddStepComponent,
    IntegrationsStepSelectComponent,
    IntegrationsStepConfigureComponent,
    IntegrationsListPage,
    IntegrationsSaveOrAddStepComponent,
    IntegrationsSelectActionComponent,
    IntegrationsSelectConnectionComponent,
    FlowViewComponent,
    FlowViewStepComponent,
    ListActionsComponent,
    StepVisiblePipe,
    CancelAddStepComponent
  ],
  providers: [CurrentFlow]
})
export class IntegrationsModule {}
