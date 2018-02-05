import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import {
  CollapseModule,
  ModalModule,
  PopoverModule,
  TabsModule,
  TooltipModule
} from 'ngx-bootstrap';
import { DataMapperModule } from '@atlasmap/atlasmap.data.mapper';
import { ActionModule, ListModule, ToolbarModule } from 'patternfly-ng';

import { IntegrationListModule } from './list';
import { IntegrationSupportModule } from './integration-support.module';
import { IntegrationDetailComponent } from './detail-page';
import { IntegrationListPage } from './list-page';

import {
  IntegrationEditPage,
  IntegrationBasicsComponent,
  IntegrationSelectConnectionComponent,
  IntegrationConfigureActionComponent,
  IntegrationSelectActionComponent,
  IntegrationSaveOrAddStepComponent,
  IntegrationStepSelectComponent,
  StepVisiblePipe,
  IntegrationStepConfigureComponent,
  DataMapperHostComponent,
  BasicFilterComponent,
  ListActionsComponent,
  CancelAddStepComponent,
  FlowViewComponent,
  FlowViewStepComponent,
  CurrentFlow
} from './edit-page';

import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';
import { ConnectionsModule } from '@syndesis/ui/connections';
import { FileUploadModule } from 'ng2-file-upload';
import { IntegrationLogsComponent } from '@syndesis/ui/integration/components/logs.component';

const editIntegrationChildRoutes = [
  { path: 'save-or-add-step', component: IntegrationSaveOrAddStepComponent },
  { path: 'integration-basics', component: IntegrationBasicsComponent },
  {
    path: 'connection-select/:position',
    component: IntegrationSelectConnectionComponent
  },
  {
    path: 'action-select/:position',
    component: IntegrationSelectActionComponent
  },
  {
    path: 'action-configure/:position/:page',
    component: IntegrationConfigureActionComponent
  },
  {
    path: 'action-configure/:position',
    component: IntegrationConfigureActionComponent
  },
  { path: 'step-select/:position', component: IntegrationStepSelectComponent },
  {
    path: 'step-configure/:position',
    component: IntegrationStepConfigureComponent
  }
];

const routes: Routes = [
  { path: '', component: IntegrationListPage, pathMatch: 'full' },
  {
    path: 'create',
    component: IntegrationEditPage,
    children: editIntegrationChildRoutes
  },
  {
    path: ':integrationId',
    component: IntegrationDetailComponent
  },
  {
    path: ':integrationId/edit',
    component: IntegrationEditPage,
    children: editIntegrationChildRoutes
  }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DynamicFormsCoreModule,
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
    ActionModule,
    ListModule,
    ToolbarModule,
    IntegrationSupportModule,
    IntegrationListModule,
  ],
  declarations: [
    DataMapperHostComponent,
    BasicFilterComponent,
    IntegrationConfigureActionComponent,
    IntegrationEditPage,
    IntegrationBasicsComponent,
    IntegrationDetailComponent,
    IntegrationSelectConnectionComponent,
    IntegrationSaveOrAddStepComponent,
    IntegrationStepSelectComponent,
    IntegrationStepConfigureComponent,
    IntegrationListPage,
    IntegrationLogsComponent,
    IntegrationSaveOrAddStepComponent,
    IntegrationSelectActionComponent,
    IntegrationSelectConnectionComponent,
    FlowViewComponent,
    FlowViewStepComponent,
    ListActionsComponent,
    StepVisiblePipe,
    CancelAddStepComponent
  ],
  providers: [
    CurrentFlow,
  ]
})
export class IntegrationModule {}
