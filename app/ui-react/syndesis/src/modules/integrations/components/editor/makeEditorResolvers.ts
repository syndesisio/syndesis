/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { IFormValue } from '@syndesis/auto-form';
import { ConnectionOverview, Integration, StepKind } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import { configureIndexMapper } from '../../resolvers';
import {
  DataShapeDirection,
  IApiProviderEditorRouteState,
  IApiProviderReviewActionsRouteState,
  IBaseApiProviderRouteParams,
  IBaseApiProviderRouteState,
  IChoiceStepRouteParams,
  IChoiceStepRouteState,
  IConfigureActionRouteParams,
  IConfigureActionRouteState,
  IConfigureStepRouteParams,
  IConfigureStepRouteState,
  IDataMapperRouteParams,
  IDataMapperRouteState,
  IDescribeDataShapeRouteParams,
  IDescribeDataShapeRouteState,
  IDescribeStepDataShapeRouteParams,
  IDescribeStepDataShapeRouteState,
  IRuleFilterStepRouteParams,
  IRuleFilterStepRouteState,
  ISelectActionRouteParams,
  ISelectActionRouteState,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  ITemplateStepRouteParams,
  ITemplateStepRouteState,
  stepRoutes,
} from './interfaces';

export interface IEditorBase {
  integration: Integration;
}

export interface IEditorIndex extends IEditorBase {
  flowId: string;
}

export interface IEditorWithOptionalFlow extends IEditorBase {
  flowId?: string;
}

export interface IEditorSelectConnection extends IEditorIndex {
  position: string;
}

export interface IEditorSelectAction extends IEditorSelectConnection {
  connection: ConnectionOverview;
}

export interface IEditorConfigureAction extends IEditorSelectAction {
  actionId: string;
  configuredProperties?: IFormValue;
  page?: string;
  updatedIntegration?: Integration;
}

export interface IEditorConfigureDataShape extends IEditorSelectAction {
  step: StepKind;
  direction: DataShapeDirection;
}

export interface IEditorConfigureStepDataShape extends IEditorConfigureStep {
  direction: DataShapeDirection;
}

export interface IEditorConfigureStep extends IEditorIndex {
  position: string;
  step: StepKind;
  updatedIntegration?: Integration;
}

export const configureSelectConnectionMapper = ({
  position,
  ...rest
}: IEditorSelectConnection) => {
  const { params, state } = configureIndexMapper(rest);
  return {
    params: {
      ...params,
      position,
    } as ISelectConnectionRouteParams,
    state,
  };
};
export const configureSelectActionMapper = ({
  connection,
  ...rest
}: IEditorSelectAction) => {
  const { params, state } = configureSelectConnectionMapper(rest);
  return {
    params: {
      ...params,
      connectionId: connection.id!,
    } as ISelectActionRouteParams,
    state: {
      ...state,
      connection,
    } as ISelectActionRouteState,
  };
};
export const configureConfigureActionMapper = ({
  actionId,
  flowId,
  page,
  integration,
  updatedIntegration,
  position,
  configuredProperties,
  ...rest
}: IEditorConfigureAction) => {
  const { params, state } = configureSelectActionMapper({
    ...rest,
    flowId,
    integration,
    position,
  });
  return {
    params: {
      ...params,
      actionId,
      page: `${page || 0}`,
    } as IConfigureActionRouteParams,
    state: {
      ...state,
      configuredProperties,
      updatedIntegration,
    } as IConfigureActionRouteState,
  };
};
export const configureDescribeDataShapeMapper = ({
  direction,
  step,
  ...rest
}: IEditorConfigureDataShape) => {
  const { params, state } = configureSelectActionMapper(rest);
  return {
    params: {
      ...params,
      direction,
    } as IDescribeDataShapeRouteParams,
    state: {
      ...state,
      step,
    } as IDescribeDataShapeRouteState,
  };
};
export const configureDescribeStepDataShapeMapper = ({
  direction,
  ...rest
}: IEditorConfigureStepDataShape) => {
  const { params, state } = configureConfigureStepMapper(rest);
  return {
    params: {
      ...params,
      direction,
    } as IDescribeStepDataShapeRouteParams,
    state: {
      ...state,
    } as IDescribeStepDataShapeRouteState,
  };
};
export const configureConfigureStepMapper = ({
  position,
  step,
  updatedIntegration,
  ...rest
}: IEditorConfigureStep) => {
  const { params, state } = configureIndexMapper(rest);
  return {
    params: {
      ...params,
      position,
    } as IConfigureActionRouteParams,
    state: {
      ...state,
      step,
      updatedIntegration,
    } as IConfigureStepRouteState,
  };
};
export const configureTemplateStepMapper = ({
  position,
  step,
  updatedIntegration,
  ...rest
}: IEditorConfigureStep) => {
  const { params, state } = configureIndexMapper(rest);
  return {
    params: {
      ...params,
      position,
    } as ITemplateStepRouteParams,
    state: {
      ...state,
      step,
      updatedIntegration,
    } as ITemplateStepRouteState,
  };
};

export const configureConfigureDataMapperMapper = ({
  position,
  step,
  updatedIntegration,
  ...rest
}: IEditorConfigureStep) => {
  const { params, state } = configureIndexMapper(rest);
  return {
    params: {
      ...params,
      position,
    } as IDataMapperRouteParams,
    state: {
      ...state,
      step,
      updatedIntegration,
    } as IDataMapperRouteState,
  };
};

export interface IApiProviderConfigureStep extends IEditorSelectConnection {}
export interface IApiProviderReviewStep extends IEditorSelectConnection {
  specification: string | Integration;
}

export const apiProviderMapper = (data: IApiProviderConfigureStep) => {
  const { params, state } = configureIndexMapper(data);
  return {
    params: {
      ...params,
      position: '0',
    } as IBaseApiProviderRouteParams,
    state: state as IBaseApiProviderRouteState,
  };
};

export const apiProviderReviewActionsMapper = ({
  specification,
  ...rest
}: IApiProviderReviewStep) => {
  const { params, state } = apiProviderMapper(rest);
  return {
    params: {
      ...params,
    } as IBaseApiProviderRouteParams,
    state: {
      ...state,
      specification,
    } as IApiProviderReviewActionsRouteState,
  };
};

export const apiProviderEditorMapper = ({
  specification,
  ...rest
}: IApiProviderReviewStep) => {
  const { params, state } = apiProviderMapper(rest);
  return {
    params: {
      ...params,
    } as IBaseApiProviderRouteParams,
    state: {
      ...state,
      specification,
    } as IApiProviderEditorRouteState,
  };
};

// export type RouteResolver<T> = {
//   [K in keyof T]: T[K] extends string ? any : RouteResolver<T[K]>
// };

export function makeEditorResolvers(esr: typeof stepRoutes) {
  return {
    selectStep: makeResolver<
      IEditorSelectConnection,
      ISelectConnectionRouteParams,
      ISelectConnectionRouteState
    >(esr.selectStep, configureSelectConnectionMapper),
    connection: {
      selectAction: makeResolver<
        IEditorSelectAction,
        ISelectActionRouteParams,
        ISelectActionRouteState
      >(esr.connection.selectAction, configureSelectActionMapper),
      configureAction: makeResolver<
        IEditorConfigureAction,
        IConfigureActionRouteParams,
        IConfigureActionRouteState
      >(esr.connection.configureAction, configureConfigureActionMapper),
      describeData: makeResolver<
        IEditorConfigureDataShape,
        IDescribeDataShapeRouteParams,
        IDescribeDataShapeRouteState
      >(esr.connection.describeData, configureDescribeDataShapeMapper),
    },
    apiProvider: {
      editSpecification: makeResolver<
        IApiProviderReviewStep,
        IBaseApiProviderRouteParams,
        IApiProviderEditorRouteState
      >(esr.apiProvider.editSpecification, apiProviderEditorMapper),
      selectMethod: makeResolver<
        IApiProviderConfigureStep,
        IBaseApiProviderRouteParams,
        IBaseApiProviderRouteState
      >(esr.apiProvider.selectMethod, apiProviderMapper),
      reviewActions: makeResolver<
        IApiProviderReviewStep,
        IBaseApiProviderRouteParams,
        IApiProviderReviewActionsRouteState
      >(esr.apiProvider.reviewActions, apiProviderReviewActionsMapper),
    },
    basicFilter: makeResolver<
      IEditorConfigureStep,
      IRuleFilterStepRouteParams,
      IRuleFilterStepRouteState
    >(esr.basicFilter, configureConfigureStepMapper),
    choice: {
      configure: makeResolver<
        IEditorConfigureStep,
        IChoiceStepRouteParams,
        IChoiceStepRouteState
        >(esr.choice.configure, configureConfigureStepMapper),
      describeData: makeResolver<
        IEditorConfigureStepDataShape,
        IDescribeStepDataShapeRouteParams,
        IDescribeStepDataShapeRouteState
        >(esr.choice.describeData, configureDescribeStepDataShapeMapper),
    },
    dataMapper: makeResolver<
      IEditorConfigureStep,
      IDataMapperRouteParams,
      IDataMapperRouteState
    >(esr.dataMapper, configureConfigureDataMapperMapper),
    template: makeResolver<
      IEditorConfigureStep,
      ITemplateStepRouteParams,
      ITemplateStepRouteState
    >(esr.template, configureTemplateStepMapper),
    step: makeResolver<
      IEditorConfigureStep,
      IConfigureStepRouteParams,
      IConfigureStepRouteState
    >(esr.step, configureConfigureStepMapper),
    extension: makeResolverNoParams('todo extension'),
  };
}
