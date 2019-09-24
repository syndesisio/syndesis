/* tslint:disable:object-literal-sort-keys no-empty-interface */
import {
  ConnectionBulletinBoard,
  DataShape,
  IConnectionOverview,
  IConnector,
  Integration,
  StepKind,
} from '@syndesis/models';
import { include } from 'named-urls';
import * as React from 'react';

export interface IPageWithEditorBreadcrumb {
  getBreadcrumb: (
    title: string,
    p: IBaseRouteParams | IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => React.ReactElement;
}

/*********************************/
/********* UI MODELS *************/
/*********************************/

export enum DataShapeDirection {
  INPUT = 'input',
  OUTPUT = 'output',
}

export interface IUIStep extends StepKind {
  board?: ConnectionBulletinBoard;
  connector?: IConnector;
  isConfigRequired: boolean;
  isTechPreview: boolean;
  uiStepKind: 'api-provider' | StepKind['stepKind'];
  title: string;
  metadata: { [key: string]: any };
  inputDataShape?: DataShape;
  outputDataShape?: DataShape;
}

export interface IUIIntegrationStep extends IUIStep {
  shape: string | undefined;
  previousStepShouldDefineDataShape: boolean;
  previousStepShouldDefineDataShapePosition?: number;
  shouldAddDataMapper: boolean;
  shouldAddDefaultFlow: boolean;
  isUnclosedSplit: boolean;
  restrictedDelete: boolean;
  notConfigurable: boolean;
}

/*********************************/
/*********** ROUTES **************/
/*********************************/

export interface IBaseRouteParams {
  integrationId: string;
}

export interface IBaseFlowRouteParams extends IBaseRouteParams {
  flowId: string;
}

export interface IBaseRouteState {
  /**
   * the integration object to edit
   */
  integration: Integration;
}

/**
 * @param actionId - the ID of the action selected in the previous step.
 * @param position - the zero-based position for the new step in the integration
 * flow.
 * @param step - the configuration step when configuring a multi-page connection.
 */
export interface IConfigureStepRouteParams extends IBaseFlowRouteParams {
  position: string;
}

/**
 * @param integration - the integration object, used to render the IVP.
 * @param connection - the connection object selected in the previous step. Needed
 * to render the IVP.
 * @param updatedIntegration - when creating a link to this page, this should
 * never be set. It is used by the page itself to pass the partially configured
 * step when configuring a multi-page connection.
 */
export interface IConfigureStepRouteState extends IBaseRouteState {
  step: StepKind;
  updatedIntegration?: Integration;
}

/**
 * @param actionId - the ID of the action selected in the previous step.
 * @param position - the zero-based position for the new step in the integration
 * flow.
 * @param step - the configuration step when configuring a multi-page connection.
 */
export interface IConfigureActionRouteParams extends IBaseFlowRouteParams {
  position: string;
  actionId: string;
  page: string;
}

/**
 * @param integration - the integration object, used to render the IVP.
 * @param connection - the connection object selected in the previous step. Needed
 * to render the IVP.
 * @param updatedIntegration - when creating a link to this page, this should
 * never be set. It is used by the page itself to pass the partially configured
 * step when configuring a multi-page connection.
 */
export interface IConfigureActionRouteState extends IBaseRouteState {
  connection: IConnectionOverview;
  updatedIntegration?: Integration;
  configuredProperties: { [key: string]: string };
}

export interface IDescribeDataShapeRouteParams extends IBaseFlowRouteParams {
  actionId?: string;
  position: string;
  direction: DataShapeDirection;
}

export interface IDescribeDataShapeRouteState extends IBaseRouteState {
  step: StepKind;
  connection: IConnectionOverview;
  updatedIntegration?: Integration;
}

/**
 * @param connectionId - the ID of the connection selected in the previous step
 * @param position - the zero-based position for the new step in the integration
 * flow.
 */
export interface ISelectActionRouteParams extends IBaseFlowRouteParams {
  connectionId: string;
  position: string;
}

/**
 * @param integration - the integration object, used to render the IVP.
 * @param connection - the connection object selected in the previous step, used
 * to render the IVP.
 */
export interface ISelectActionRouteState extends IBaseRouteState {
  connection: IConnectionOverview;
}

/**
 * @param position - the zero-based position for the new step in the integration
 * flow.
 */
export interface ISelectConnectionRouteParams extends IBaseFlowRouteParams {
  position: string;
}

export interface IBaseApiProviderRouteParams
  extends ISelectConnectionRouteParams {}
export interface IBaseApiProviderRouteState
  extends IConfigureStepRouteParams,
    IBaseRouteState {}
export interface IApiProviderReviewActionsRouteState
  extends IBaseApiProviderRouteState {
  specification: string;
}
export interface IApiProviderEditorRouteState
  extends IBaseApiProviderRouteState {
  specification: string | Integration;
}

/**
 * @param mode - valid choice configuration modes.
 */
export interface IChoiceStepRouteParams extends IConfigureStepRouteParams {
  configMode: 'basic' | 'advanced'
}
export interface IChoiceStepRouteState extends IConfigureStepRouteState {}

export interface ITemplateStepRouteParams extends IConfigureStepRouteParams {}
export interface ITemplateStepRouteState extends IConfigureStepRouteState {}
export interface IDataMapperRouteParams extends IConfigureStepRouteParams {}
export interface IDataMapperRouteState extends IConfigureStepRouteState {}
export interface IRuleFilterStepRouteParams extends IConfigureStepRouteParams {}
export interface IRuleFilterStepRouteState extends IConfigureStepRouteState {}
export interface ISelectConfigModeRouteParams extends IConfigureStepRouteParams {}
export interface ISelectConfigModeRouteState extends IConfigureStepRouteState {}
export interface ISelectConnectionRouteState extends IBaseRouteState {}
export interface IPostPublishRouteParams extends IBaseRouteParams {}
export interface ISaveIntegrationRouteParams extends IBaseRouteParams {}
export interface ISaveIntegrationRouteState extends IBaseRouteState {
  flowId?: string;
}

export const stepRoutes = {
  // step 1
  selectStep: '',
  // if selected step is api provider
  apiProvider: include('api-provider', {
    selectMethod: '',
    reviewActions: 'review-actions',
    editSpecification: 'edit-specification',
  }),
  // if selected step kind is data mapper
  dataMapper: 'mapper',
  // if selected step kind is basic filter
  basicFilter: 'filter',
  // if selected step kind is choice
  choice: include('choice', {
    selectMode: '',
    configure: 'configure/:configMode',
    // if 'any' data shape
    describeData: 'describe-data',
  }),
  // if selected step kind is template
  template: 'template',
  // if selected step kind is step
  step: 'step',
  // if selected step kind is extension
  extension: 'extension',
  // if selected step kind is endpoint
  connection: include('connection/:connectionId', {
    selectAction: '',
    configureAction: ':actionId/:page',
    // if 'any' data shape
    describeData: 'describe-data/:position/:direction',
  }),
};
/**
 * Both the integration creator and editor share the same routes when the creator
 * reaches the third step in the wizard. This object is to keep them DRY.
 */
export const editorRoutes = include('editor', {
  index: ':flowId/add-step',
  operations: 'operations',
  addStep: include(':flowId/:position/add', stepRoutes),
  editStep: include(':flowId/:position/edit', stepRoutes),
  saveAndPublish: 'save',
  entryPoint: '',
  root: '',
});
