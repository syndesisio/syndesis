import { forwardRef, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { DataMapperModule } from '@atlasmap/atlasmap-data-mapper';

import { VendorModule } from '@syndesis/ui/vendor';
import {
  OpenApiModule,
  PatternflyUIModule,
  SyndesisCommonModule,
} from '@syndesis/ui/common';
import { ConnectionsModule } from '@syndesis/ui/connections';

import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { IntegrationListModule } from '@syndesis/ui/integration/list';
import { IntegrationListPage } from '@syndesis/ui/integration/list-page';
import { IntegrationImportPageComponent } from '@syndesis/ui/integration/import-page';
import {
  INTEGRATION_DETAIL_DIRECTIVES,
  IntegrationDetailComponent,
} from '@syndesis/ui/integration/integration_detail';
import { IntegrationLogsComponent } from '@syndesis/ui/integration/integration_logs';
import {
  ApiProviderEffects,
  apiProviderEndpoints,
  ApiProviderOperationsEditorComponent,
  ApiProviderOperationsComponent,
  ApiProviderOperationsListComponent,
  ApiProviderOperationsToolbarComponent,
  apiProviderReducer,
  ApiProviderSpecComponent,
  StepEditorComponent,
  StepNameComponent,
  StepUploadComponent,
  StepValidateComponent,
} from '@syndesis/ui/integration/api-provider';

import {
  BasicFilterComponent,
  ContentBasedRouterComponent,
  CurrentFlowService,
  DataMapperHostComponent,
  FlowPageService,
  FlowPageHeaderComponent,
  FlowToolbarComponent,
  FlowViewComponent,
  FlowViewCancelPromptComponent,
  FlowViewStepComponent,
  IntegrationBasicsComponent,
  IntegrationConfigureActionComponent,
  IntegrationDescribeDataComponent,
  IntegrationEditPage,
  IntegrationSaveOrAddStepComponent,
  IntegrationSelectActionComponent,
  IntegrationSelectStepComponent,
  IntegrationStepConfigureComponent,
  ListActionsComponent,
  StepVisiblePipe,
  TemplaterComponent,
} from '@syndesis/ui/integration/edit-page';
import { ApiModule } from '@syndesis/ui/api';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { WindowRef } from '@syndesis/ui/customizations/window-ref';
import { ApiProviderService } from '@syndesis/ui/integration/api-provider/api-provider.service';
import { FlowViewMultiFlowComponent } from '@syndesis/ui/integration/edit-page/flow-view/flow-view-multiflow.component';
import { ApiConnectorGuard } from '@syndesis/ui/integration/api-provider/api-provider.guard';
import { ApiProviderSpecificationEditorPage } from './api-provider/operations-page/specification/specification-editor-page.component';
import { IntegrationResolverService } from './edit-page/integration-resolver.service';
import { StepsResolverService } from './edit-page/connection-resolver.service';
// tslint:disable-next-line:max-line-length
import { ApiProviderOperationDescriptionComponent } from './api-provider/operations-page/integration-api-provider-operation-description-component';
import { CanDeactivateGuard } from '@syndesis/ui/platform';

const syndesisCommonModuleFwd = forwardRef(() => SyndesisCommonModule);
const integrationSupportModuleFwd = forwardRef(() => IntegrationSupportModule);
const integrationListModuleFwd = forwardRef(() => IntegrationListModule);

const editIntegrationChildRoutes = [
  {
    path: 'save-or-add-step',
    component: IntegrationSaveOrAddStepComponent,
  },
  {
    path: 'integration-basics',
    component: IntegrationBasicsComponent,
  },
  {
    path: 'step-select/:position',
    component: IntegrationSelectStepComponent,
    resolve: {
      steps: StepsResolverService,
    },
  },
  {
    path: 'action-select/:position',
    component: IntegrationSelectActionComponent,
  },
  {
    path: 'action-configure/:position/:page',
    component: IntegrationConfigureActionComponent,
  },
  {
    path: 'action-configure/:position',
    component: IntegrationConfigureActionComponent,
  },
  {
    path: 'describe-data/:position',
    redirectTo: 'describe-data/:position/input',
  },
  {
    path: 'describe-data/:position/:direction',
    component: IntegrationDescribeDataComponent,
  },
  {
    path: 'step-configure/:position',
    component: IntegrationStepConfigureComponent,
  },
  // OpenAPI loader page
  {
    path: 'api-provider/create',
    component: ApiProviderSpecComponent,
    canActivate: [ApiConnectorGuard],
  },
];

const routes: Routes = [
  {
    path: '',
    component: IntegrationListPage,
    pathMatch: 'full',
  },
  {
    path: 'import',
    component: IntegrationImportPageComponent,
  },
  {
    path: 'create',
    component: IntegrationEditPage,
    children: editIntegrationChildRoutes,
    resolve: {
      integration: IntegrationResolverService,
    },
    canDeactivate: [CanDeactivateGuard],
  },
  {
    path: ':integrationId',
    component: IntegrationDetailComponent,
  },
  {
    path: ':integrationId/edit',
    component: IntegrationEditPage,
    resolve: {
      integration: IntegrationResolverService,
    },
  },
  {
    path: ':integrationId/operations',
    component: ApiProviderOperationsComponent,
    resolve: {
      integration: IntegrationResolverService,
    },
  },
  {
    path: ':integrationId/operations/edit',
    component: ApiProviderOperationsEditorComponent,
    resolve: {
      integration: IntegrationResolverService,
    },
  },
  {
    path: ':integrationId/operations/:flowId/edit',
    component: ApiProviderOperationsEditorComponent,
    children: editIntegrationChildRoutes,
    resolve: {
      integration: IntegrationResolverService,
    },
    canDeactivate: [CanDeactivateGuard],
  },
  {
    path: ':integrationId/:flowId/edit',
    component: IntegrationEditPage,
    children: editIntegrationChildRoutes,
    resolve: {
      integration: IntegrationResolverService,
    },
    canDeactivate: [CanDeactivateGuard],
  },
  {
    path: ':integrationId/specification',
    component: ApiProviderSpecificationEditorPage,
  },
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DynamicFormsCoreModule,
    PatternflyUIModule,
    RouterModule.forChild(routes),
    ApiModule.forChild(apiProviderEndpoints),
    StoreModule.forFeature('apiProviderState', apiProviderReducer),
    EffectsModule.forFeature([ApiProviderEffects]),
    ConnectionsModule,
    VendorModule,
    syndesisCommonModuleFwd,
    DataMapperModule,
    integrationSupportModuleFwd,
    integrationListModuleFwd,
    OpenApiModule,
  ],
  declarations: [
    ...INTEGRATION_DETAIL_DIRECTIVES,
    DataMapperHostComponent,
    BasicFilterComponent,
    ContentBasedRouterComponent,
    TemplaterComponent,
    IntegrationConfigureActionComponent,
    IntegrationEditPage,
    IntegrationBasicsComponent,
    IntegrationDetailComponent,
    IntegrationDescribeDataComponent,
    IntegrationSelectStepComponent,
    IntegrationSaveOrAddStepComponent,
    IntegrationStepConfigureComponent,
    IntegrationListPage,
    IntegrationImportPageComponent,
    IntegrationLogsComponent,
    IntegrationSaveOrAddStepComponent,
    IntegrationSelectActionComponent,
    FlowPageHeaderComponent,
    FlowToolbarComponent,
    FlowViewComponent,
    FlowViewCancelPromptComponent,
    FlowViewStepComponent,
    FlowViewMultiFlowComponent,
    ListActionsComponent,
    StepVisiblePipe,
    ApiProviderOperationsComponent,
    ApiProviderOperationsEditorComponent,
    ApiProviderOperationsListComponent,
    ApiProviderOperationsToolbarComponent,
    ApiProviderOperationDescriptionComponent,
    ApiProviderSpecComponent,
    StepUploadComponent,
    StepValidateComponent,
    StepEditorComponent,
    StepNameComponent,
    ApiProviderSpecificationEditorPage,
  ],
  providers: [
    CurrentFlowService,
    FlowPageService,
    WindowRef,
    ApiProviderService,
    ApiConnectorGuard,
    StepVisiblePipe,
  ],
})
export class IntegrationModule {}
