/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { getStep } from '@syndesis/api';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import { configureIndexMapper } from '../../resolvers';
import {
  IConfigureActionRouteParams,
  IConfigureActionRouteState,
  IConfigureStepRouteParams,
  IConfigureStepRouteState,
  IDataMapperRouteParams,
  IDataMapperRouteState,
  IDescribeDataShapeRouteParams,
  IDescribeDataShapeRouteState,
  IEditorConfigureAction,
  IEditorConfigureDataShape,
  IEditorConfigureStep,
  IEditorSelectAction,
  IEditorSelectConnection,
  ISelectActionRouteParams,
  ISelectActionRouteState,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  ITemplateStepRouteParams,
  ITemplateStepRouteState,
  stepRoutes,
} from './interfaces';

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
  step,
  integration,
  updatedIntegration,
  position,
  ...rest
}: IEditorConfigureAction) => {
  const { params, state } = configureSelectActionMapper({
    ...rest,
    flowId,
    integration,
    position,
  });
  const positionAsNumber = parseInt(position, 10);
  const stepObject = getStep(integration, flowId, positionAsNumber) || {};
  return {
    params: {
      ...params,
      actionId,
      step: `${step || 0}`,
    } as IConfigureActionRouteParams,
    state: {
      ...state,
      updatedIntegration,
      configuredProperties: stepObject.configuredProperties || {},
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
      upload: makeResolver<
        IEditorConfigureStep,
        IConfigureStepRouteParams,
        IConfigureStepRouteState
        >(esr.apiProvider.upload, configureConfigureStepMapper),
      review: makeResolverNoParams('todo review'),
      edit: makeResolverNoParams('todo edit'),
    },
    basicFilter: makeResolverNoParams('todo basicFilter'),
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
