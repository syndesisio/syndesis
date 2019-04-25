/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { getEmptyIntegration, getStep } from '@syndesis/api';
import { ConnectionOverview, Integration, Step } from '@syndesis/models';
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
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState,
  ISelectActionRouteParams,
  ISelectActionRouteState,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
} from './components/editor/interfaces';
import {
  IActivityPageParams,
  IActivityPageState,
  IDetailsPageParams,
  IDetailsPageState,
  IMetricsPageParams,
  IMetricsPageState,
} from './pages/detail';
import routes from './routes';

interface IEditorIndex {
  flow: string;
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

export const configureIndexMapper = ({ flow, integration }: IEditorIndex) => ({
  params: {
    flow,
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
  step,
  integration,
  updatedIntegration,
  position,
  ...rest
}: IEditorConfigureAction) => {
  const { params, state } = configureSelectActionMapper({
    ...rest,
    integration,
    position,
  });
  const positionAsNumber = parseInt(position, 10);
  const stepObject = getStep(integration, 0, positionAsNumber) || {};
  return {
    params: {
      ...params,
      actionId,
      step: `${step || 0}`,
    } as IConfigureActionRouteParams,
    state: {
      ...state,
      updatedIntegration,
      configuredProperties: stepObject.configuredProperties,
    } as IConfigureActionRouteState,
  };
};

// TODO: unit test every single one of these resolvers 😫

export const listResolver = makeResolverNoParams(routes.list);

export const manageCicdResolver = makeResolverNoParams(routes.manageCicd.root);

export const createRootResolver = makeResolverNoParams(routes.create.root);

export const createStartSelectStepResolver = makeResolverNoParamsWithDefaults<
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState
>(routes.create.start.selectStep, () => {
  return {
    params: {
      flow: '0',
      position: '0',
    },
    state: {
      integration: getEmptyIntegration(),
    },
  };
});

export const createStartSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectActionRouteState
>(routes.create.start.connection.selectAction, configureSelectActionMapper);

export const createStartConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.create.start.connection.configureAction,
  configureConfigureActionMapper
);

export const createFinishSelectStepResolver = makeResolver<
  IEditorSelectConnection,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState
>(routes.create.finish.selectStep, configureSelectConnectionMapper);

export const createFinishSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectActionRouteState
>(routes.create.finish.connection.selectAction, configureSelectActionMapper);

export const createFinishConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.create.finish.connection.configureAction,
  configureConfigureActionMapper
);

export const createConfigureIndexResolver = makeResolver<
  IEditorIndex,
  IBaseRouteParams,
  IBaseRouteState
>(routes.create.configure.index, configureIndexMapper);

export const createConfigureAddStepSelectStepResolver = makeResolver<
  IEditorSelectConnection,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState
>(routes.create.configure.addStep.selectStep, configureSelectConnectionMapper);

export const createConfigureAddStepSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectActionRouteState
>(
  routes.create.configure.addStep.connection.selectAction,
  configureSelectActionMapper
);

export const createConfigureAddStepConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.create.configure.addStep.connection.configureAction,
  configureConfigureActionMapper
);

export const createConfigureEditStepSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectActionRouteState
>(
  routes.create.configure.editStep.connection.selectAction,
  configureSelectActionMapper
);

export const createConfigureEditStepConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.create.configure.editStep.connection.configureAction,
  configureConfigureActionMapper
);

export const createConfigureEditStepSaveAndPublishResolver = makeResolver<
  { integration: Integration },
  null,
  ISaveIntegrationRouteState
>(routes.create.configure.saveAndPublish, ({ integration }) => ({
  params: null,
  state: {
    integration,
  },
}));

export const integrationActivityResolver = makeResolver<
  { integration: Integration },
  IActivityPageParams,
  IActivityPageState
>(routes.integration.activity, ({ integration }) => ({
  params: {
    integrationId: integration.id!,
  },
  state: {
    integration,
  },
}));

export const integrationDetailsResolver = makeResolver<
  { integration: Integration },
  IDetailsPageParams,
  IDetailsPageState
>(routes.integration.details, ({ integration }) => ({
  params: {
    integrationId: integration.id!,
  },
  state: {
    integration,
  },
}));

export const integrationEditIndexResolver = makeResolver<
  IEditorIndex,
  IBaseRouteParams,
  IBaseRouteState
>(routes.integration.edit.index, configureIndexMapper);

export const integrationEditAddStepSelectStepResolver = makeResolver<
  IEditorSelectConnection,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState
>(routes.integration.edit.addStep.selectStep, configureSelectConnectionMapper);

export const integrationEditAddStepSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectConnectionRouteState
>(
  routes.integration.edit.addStep.connection.selectAction,
  configureSelectActionMapper
);

export const integrationEditAddStepConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.integration.edit.addStep.connection.configureAction,
  configureConfigureActionMapper
);

export const integrationEditEditStepSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectActionRouteState
>(
  routes.integration.edit.editStep.connection.selectAction,
  configureSelectActionMapper
);

export const integrationEditEditStepConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.integration.edit.editStep.connection.configureAction,
  configureConfigureActionMapper
);

export const integrationEditSaveAndPublish = makeResolver<
  IEditorIndex,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState
>(routes.integration.edit.saveAndPublish, configureIndexMapper);

export const metricsResolver = makeResolver<
  { integration: Integration },
  IMetricsPageParams,
  IMetricsPageState
>(routes.integration.metrics, ({ integration }) => ({
  params: {
    integrationId: integration.id!,
  },
  state: {
    integration,
  },
}));

/**
 * A special resolver, this will return a different url depending on the step kind
 */
export const getStepKind = (stepOrConnection: ConnectionOverview | Step) => {
  if ((stepOrConnection as ConnectionOverview).connectorId === 'api-provider') {
    return 'api-provider';
  }
  if ((stepOrConnection as Step).stepKind) {
    // not a connection
  }
  return 'endpoint';
};

export const createStartStepSwitcherResolver = makeResolver<{
  connection: ConnectionOverview | Step;
  params: ISelectConnectionRouteParams;
  state: ISelectConnectionRouteState;
}>(
  '',
  ({ connection, params, state }): any => {
    const stepKind = getStepKind(connection);
    switch (stepKind) {
      case 'api-provider':
        return makeResolverNoParams(
          routes.create.start.apiProvider.specification
        );
      default:
        return createStartSelectActionResolver({
          connection: connection as ConnectionOverview,
          ...params,
          ...state,
        });
    }
  }
);

export const createFinishStepSwitcherResolver = makeResolver<{
  connection: ConnectionOverview | Step;
  params: ISelectConnectionRouteParams;
  state: ISelectConnectionRouteState;
}>(
  '',
  ({ connection, params, state }): any => {
    const stepKind = getStepKind(connection);
    switch (stepKind) {
      case 'api-provider':
        return makeResolverNoParams(
          routes.create.finish.apiProvider.specification
        );
      default:
        return createFinishSelectActionResolver({
          connection: connection as ConnectionOverview,
          ...params,
          ...state,
        });
    }
  }
);

export const createConfigureAddStepStepSwitcherResolver = makeResolver<
  IEditorSelectAction
>(
  '',
  ({ connection, ...rest }): any => {
    const stepKind = getStepKind(connection);
    switch (stepKind) {
      case 'api-provider':
        return makeResolverNoParams(
          routes.create.configure.addStep.apiProvider.specification
        );
      default:
        return createConfigureAddStepSelectActionResolver({
          ...rest,
          connection: connection as ConnectionOverview,
        });
    }
  }
);

export const integrationEditAddStepStepSwitcherResolver = makeResolver<
  IEditorSelectAction
>(
  '',
  ({ connection, ...rest }): any => {
    const stepKind = getStepKind(connection);
    switch (stepKind) {
      case 'api-provider':
        return makeResolverNoParams(
          routes.create.configure.editStep.apiProvider.specification
        );
      default:
        return integrationEditAddStepSelectActionResolver({
          ...rest,
          connection: connection as ConnectionOverview,
        });
    }
  }
);

export default {
  list: listResolver,
  manageCicd: {
    root: manageCicdResolver,
  },
  create: {
    root: createRootResolver,
    start: {
      selectStep: createStartSelectStepResolver,
      stepSwitcher: createStartStepSwitcherResolver,
      selectAction: createStartSelectActionResolver,
      configureAction: createStartConfigureActionResolver,
    },
    finish: {
      selectStep: createFinishSelectStepResolver,
      stepSwitcher: createFinishStepSwitcherResolver,
      selectAction: createFinishSelectActionResolver,
      configureAction: createFinishConfigureActionResolver,
    },
    configure: {
      index: createConfigureIndexResolver,
      addStep: {
        selectStep: createConfigureAddStepSelectStepResolver,
        stepSwitcher: createConfigureAddStepStepSwitcherResolver,
        selectAction: createConfigureAddStepSelectActionResolver,
        configureAction: createConfigureAddStepConfigureActionResolver,
      },
      editStep: {
        selectAction: createConfigureEditStepSelectActionResolver,
        configureAction: createConfigureEditStepConfigureActionResolver,
      },
      saveAndPublish: createConfigureEditStepSaveAndPublishResolver,
    },
  },
  integration: {
    activity: integrationActivityResolver,
    details: integrationDetailsResolver,
    edit: {
      index: integrationEditIndexResolver,
      addStep: {
        selectStep: integrationEditAddStepSelectStepResolver,
        stepSwitcher: integrationEditAddStepStepSwitcherResolver,
        selectAction: integrationEditAddStepSelectActionResolver,
        configureAction: integrationEditAddStepConfigureActionResolver,
      },
      editStep: {
        selectAction: integrationEditEditStepSelectActionResolver,
        configureAction: integrationEditEditStepConfigureActionResolver,
      },
      saveAndPublish: integrationEditSaveAndPublish,
    },
    metrics: metricsResolver,
  },
};
