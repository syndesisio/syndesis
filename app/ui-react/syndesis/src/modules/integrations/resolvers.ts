/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { getEmptyIntegration, getStep } from '@syndesis/api';
import {
  ConnectionOverview,
  IIntegrationOverviewWithDraft,
  Integration,
  StepKind,
} from '@syndesis/models';
import {
  makeResolver,
  makeResolverNoParams,
  makeResolverNoParamsWithDefaults,
} from '@syndesis/utils';
import {
  IBaseRouteParams,
  IBaseRouteState,
  IConfigureActionRouteParams,
  IConfigureActionRouteState,
  IConfigureStepRouteParams,
  IConfigureStepRouteState,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState,
  ISelectActionRouteParams,
  ISelectActionRouteState,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  ITemplateStepRouteParams,
  ITemplateStepRouteState,
} from './components/editor/interfaces';
import {
  IDetailsRouteParams,
  IDetailsRouteState,
} from './pages/detail/interfaces';
import routes, { stepRoutes } from './routes';

interface IEditorIndex {
  flowId: string;
  integration: Integration;
}

interface IEditorSelectConnection extends IEditorIndex {
  position: string;
}

interface IEditorSelectAction extends IEditorSelectConnection {
  connection: ConnectionOverview;
}

interface IEditorConfigureAction extends IEditorSelectAction {
  actionId: string;
  step?: string;
  updatedIntegration?: Integration;
}

interface IEditorConfigureStep extends IEditorIndex {
  position: string;
  step: StepKind;
  updatedIntegration?: Integration;
}

export const configureIndexMapper = ({
  flowId,
  integration,
}: IEditorIndex) => ({
  params: {
    flowId: flowId ? flowId : integration.flows![0].id!,
    ...(integration && integration.id ? { integrationId: integration.id } : {}),
  } as IBaseRouteParams,
  state: {
    integration,
  } as IBaseRouteState,
});

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

export function makeEditorStepRoutesResolvers(
  esr: typeof stepRoutes
): RouteResolver<typeof stepRoutes> {
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
        ISelectConnectionRouteState
      >(esr.connection.selectAction, configureSelectActionMapper),
      configureAction: makeResolver<
        IEditorConfigureAction,
        IConfigureActionRouteParams,
        IConfigureActionRouteState
      >(esr.connection.configureAction, configureConfigureActionMapper),
      describeData: () => 'describeData',
    },
    apiProvider: {
      upload: makeResolverNoParams(esr.apiProvider.upload),
      review: () => 'review',
      edit: () => 'edit',
    },
    basicFilter: () => 'basicFilter',
    dataMapper: () => 'dataMapper',
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
    extension: () => 'extension',
  };
}

// TODO: unit test every single one of these resolvers 😫

export const listResolver = makeResolverNoParams(routes.list);

export const manageCicdResolver = makeResolverNoParams(routes.manageCicd.root);

export const integrationActivityResolver = makeResolver<
  { integrationId: string; integration?: IIntegrationOverviewWithDraft },
  IDetailsRouteParams,
  IDetailsRouteState
>(routes.integration.activity, ({ integrationId, integration }) => ({
  params: {
    integrationId,
  },
  state: {
    integration,
  },
}));

export const integrationDetailsResolver = makeResolver<
  { integrationId: string; integration?: IIntegrationOverviewWithDraft },
  IDetailsRouteParams,
  IDetailsRouteState
>(routes.integration.details, ({ integrationId, integration }) => ({
  params: {
    integrationId,
  },
  state: {
    integration,
  },
}));

export const metricsResolver = makeResolver<
  { integrationId: string; integration?: IIntegrationOverviewWithDraft },
  IDetailsRouteParams,
  IDetailsRouteState
>(routes.integration.metrics, ({ integrationId, integration }) => ({
  params: {
    integrationId,
  },
  state: {
    integration,
  },
}));

type RouteResolver<T> = {
  [K in keyof T]: T[K] extends string ? any : RouteResolver<T[K]>
};

const resolvers: RouteResolver<typeof routes> = {
  list: listResolver,
  manageCicd: {
    root: manageCicdResolver,
  },
  create: {
    root: makeResolverNoParams(routes.create.root),
    start: {
      ...makeEditorStepRoutesResolvers(routes.create.start),
      selectStep: makeResolverNoParamsWithDefaults<
        ISelectConnectionRouteParams,
        ISelectConnectionRouteState
      >(routes.create.start.selectStep, () => {
        const integration = getEmptyIntegration();
        return {
          params: {
            flowId: integration.flows![0].id!,
            position: '0',
          },
          state: {
            integration,
          },
        };
      }),
    },
    finish: makeEditorStepRoutesResolvers(routes.create.finish),
    configure: {
      root: makeResolverNoParams(routes.create.configure.root),
      index: makeResolver<IEditorIndex, IBaseRouteParams, IBaseRouteState>(
        routes.create.configure.index,
        configureIndexMapper
      ),
      addStep: makeEditorStepRoutesResolvers(routes.create.configure.addStep),
      editStep: makeEditorStepRoutesResolvers(routes.create.configure.editStep),
      saveAndPublish: makeResolver<
        IEditorIndex,
        ISaveIntegrationRouteParams,
        ISaveIntegrationRouteState
      >(routes.create.configure.saveAndPublish, configureIndexMapper),
    },
  },
  integration: {
    root: makeResolverNoParams(routes.integration.root),
    activity: integrationActivityResolver,
    details: integrationDetailsResolver,
    edit: {
      root: makeResolverNoParams(routes.integration.edit.root),
      index: makeResolver<IEditorIndex, IBaseRouteParams, IBaseRouteState>(
        routes.integration.edit.index,
        configureIndexMapper
      ),
      addStep: makeEditorStepRoutesResolvers(routes.integration.edit.addStep),
      editStep: makeEditorStepRoutesResolvers(routes.integration.edit.editStep),
      saveAndPublish: makeResolver<
        IEditorIndex,
        ISaveIntegrationRouteParams,
        ISaveIntegrationRouteState
      >(routes.integration.edit.saveAndPublish, configureIndexMapper),
    },
    metrics: metricsResolver,
  },
  import: makeResolverNoParams(routes.import),
};

export default resolvers;
