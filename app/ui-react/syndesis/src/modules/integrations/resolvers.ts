/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { getStep } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import { Step } from '@syndesis/models/src';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import {
  IFinishActionRouteParams,
  IFinishActionRouteState,
  IFinishConfigurationPageRouteParams,
  IFinishConfigurationPageRouteState,
  IFinishConnectionRouteState,
} from './pages/create/finish';
import {
  IStartActionRouteParams,
  IStartActionRouteState,
  IStartConfigurationPageRouteParams,
  IStartConfigurationPageRouteState,
} from './pages/create/start';
import {
  IActivityPageParams,
  IActivityPageState,
  IDetailsPageParams,
  IDetailsPageState,
  IMetricsPageParams,
  IMetricsPageState,
} from './pages/detail';
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
} from './pages/editorInterfaces';
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
  step?: number;
  updatedIntegration?: Integration;
}

export const configureIndexMapper = ({ flow, integration }: IEditorIndex) => ({
  params: {
    flow,
    integrationId: integration ? integration.id : undefined,
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
  const stepObject = getStep(integration, 0, positionAsNumber);
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

export const createStartSelectActionResolver = makeResolver<
  { connection: ConnectionOverview },
  IStartActionRouteParams,
  IStartActionRouteState
>(routes.create.start.connection.selectAction, ({ connection }) => ({
  params: {
    connectionId: connection.id!,
  },
  state: {
    connection,
  },
}));

// TODO: unit test every single one of these resolvers 😫

export const listResolver = makeResolverNoParams(routes.list);

export const manageCicdResolver = makeResolverNoParams(routes.manageCicd.root);

export const createRootResolver = makeResolverNoParams(routes.create.root);

export const createStartSelectStepResolver = makeResolverNoParams(
  routes.create.start.selectStep
);

export const createStartConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IStartConfigurationPageRouteParams,
  IStartConfigurationPageRouteState
>(
  routes.create.start.connection.configureAction,
  ({ connection, integration, actionId, step, updatedIntegration }) => ({
    params: {
      connectionId: connection.id,
      actionId,
      step: `${step || 0}`,
    },
    state: {
      connection,
      integration,
      updatedIntegration,
    },
  })
);

export const createFinishSelectStepResolver = makeResolver<
  {
    integration: Integration;
    startConnection: ConnectionOverview;
    startAction: Action;
  },
  null,
  IFinishConnectionRouteState
>(
  routes.create.finish.selectStep,
  ({ integration, startConnection, startAction }) => ({
    params: null,
    state: {
      integration,
      startAction,
      startConnection,
    },
  })
);

export const createFinishSelectActionResolver = makeResolver<
  {
    integration: Integration;
    startConnection: ConnectionOverview;
    startAction: Action;
    finishConnection: ConnectionOverview;
  },
  IFinishActionRouteParams,
  IFinishActionRouteState
>(
  routes.create.finish.connection.selectAction,
  ({ integration, startConnection, startAction, finishConnection }) => ({
    params: {
      connectionId: finishConnection.id!,
    },
    state: {
      integration,
      startAction,
      startConnection,
      finishConnection,
    },
  })
);

export const createFinishConfigureActionResolver = makeResolver<
  {
    integration: Integration;
    updatedIntegration: Integration;
    startConnection: ConnectionOverview;
    startAction: Action;
    finishConnection: ConnectionOverview;
    actionId: string;
    step?: number;
  },
  IFinishConfigurationPageRouteParams,
  IFinishConfigurationPageRouteState
>(
  routes.create.finish.connection.configureAction,
  ({
    integration,
    startConnection,
    startAction,
    finishConnection,
    actionId,
    step,
    updatedIntegration,
  }) => ({
    params: {
      actionId,
      connectionId: finishConnection.id,
      step: `${step || 0}`,
    },
    state: {
      integration,
      updatedIntegration,
      startAction,
      startConnection,
      finishConnection,
    },
  })
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
  stepOrConnection: ConnectionOverview | Step;
}>(
  '',
  ({ stepOrConnection }): any => {
    const stepKind = getStepKind(stepOrConnection);
    switch (stepKind) {
      case 'api-provider':
        return makeResolverNoParams(
          routes.create.start.apiProvider.specification
        );
      default:
        return createStartSelectActionResolver({
          connection: stepOrConnection as ConnectionOverview,
        });
    }
  }
);

export const createFinishStepSwitcherResolver = makeResolver<{
  integration: Integration;
  startConnection: ConnectionOverview;
  startAction: Action;
  finishConnection: ConnectionOverview | Step;
}>(
  '',
  ({ finishConnection, ...rest }): any => {
    const stepKind = getStepKind(finishConnection);
    switch (stepKind) {
      case 'api-provider':
        return makeResolverNoParams(
          routes.create.finish.apiProvider.specification
        );
      default:
        return createFinishSelectActionResolver({
          ...rest,
          finishConnection: finishConnection as ConnectionOverview,
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
