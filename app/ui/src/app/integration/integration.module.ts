import { NgModule, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { DataMapperModule } from '@atlasmap/atlasmap-data-mapper';

import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisCommonModule, PatternflyUIModule } from '@syndesis/ui/common';
import { ConnectionsModule } from '@syndesis/ui/connections';

import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { IntegrationListModule } from '@syndesis/ui/integration/list';
import { IntegrationListPage } from '@syndesis/ui/integration/list-page';
import { IntegrationImportPageComponent } from '@syndesis/ui/integration/import-page';
import {
  IntegrationDetailComponent,
  INTEGRATION_DETAIL_DIRECTIVES
} from '@syndesis/ui/integration/integration_detail';
import { IntegrationLogsComponent } from '@syndesis/ui/integration/integration_logs';

import {
  IntegrationEditPage,
  IntegrationBasicsComponent,
  IntegrationSelectConnectionComponent,
  IntegrationConfigureActionComponent,
  IntegrationSelectActionComponent,
  IntegrationSaveOrAddStepComponent,
  IntegrationStepSelectComponent,
  IntegrationDescribeDataComponent,
  StepVisiblePipe,
  IntegrationStepConfigureComponent,
  DataMapperHostComponent,
  BasicFilterComponent,
  ListActionsComponent,
  CancelAddStepComponent,
  FlowViewComponent,
  FlowViewStepComponent,
  CurrentFlowService,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';

const syndesisCommonModuleFwd = forwardRef(() => SyndesisCommonModule);
const integrationSupportModuleFwd = forwardRef(() => IntegrationSupportModule);
const integrationListModuleFwd = forwardRef(() => IntegrationListModule);

const editIntegrationChildRoutes = [
  {
    path: 'save-or-add-step',
    component: IntegrationSaveOrAddStepComponent
  },
  {
    path: 'integration-basics',
    component: IntegrationBasicsComponent
  },
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
  {
    path: 'describe-data/:position',
    redirectTo: 'describe-data/:position/input'
  },
  {
    path: 'describe-data/:position/:direction',
    component: IntegrationDescribeDataComponent
  },
  { path: 'step-select/:position', component: IntegrationStepSelectComponent },
  {
    path: 'step-configure/:position',
    component: IntegrationStepConfigureComponent
  }
];

const routes: Routes = [
  {
    path: '',
    component: IntegrationListPage,
    pathMatch: 'full'
  },
  {
    path: 'import',
    component: IntegrationImportPageComponent,
  },
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
    VendorModule,
    syndesisCommonModuleFwd,
    DataMapperModule,
    integrationSupportModuleFwd,
    integrationListModuleFwd,
  ],
  declarations: [
    ...INTEGRATION_DETAIL_DIRECTIVES,
    DataMapperHostComponent,
    BasicFilterComponent,
    IntegrationConfigureActionComponent,
    IntegrationEditPage,
    IntegrationBasicsComponent,
    IntegrationDetailComponent,
    IntegrationDescribeDataComponent,
    IntegrationSelectConnectionComponent,
    IntegrationSaveOrAddStepComponent,
    IntegrationStepSelectComponent,
    IntegrationStepConfigureComponent,
    IntegrationListPage,
    IntegrationImportPageComponent,
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
  providers: [CurrentFlowService, FlowPageService]
})
export class IntegrationModule {}
