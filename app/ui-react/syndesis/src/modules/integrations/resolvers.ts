/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { getStep } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
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

const configureIndexMapper = ({ flow, integration }: IEditorIndex) => ({
  params: {
    flow,
    integrationId: integration ? integration.id : undefined,
  } as IBaseRouteParams,
  state: {
    integration,
  } as IBaseRouteState,
});

const configureSelectConnectionMapper = ({
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

const configureSelectActionMapper = ({
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

const configureConfigureActionMapper = ({
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

// TODO: unit test every single one of these resolvers 😫
export default {
  list: makeResolverNoParams(routes.list),
  manageCicd: {
    root: makeResolverNoParams(routes.manageCicd.root),
  },
  create: {
    root: makeResolverNoParams(routes.create.root),
    start: {
      selectConnection: makeResolverNoParams(
        routes.create.start.selectConnection
      ),
      selectAction: makeResolver<
        { connection: ConnectionOverview },
        IStartActionRouteParams,
        IStartActionRouteState
      >(routes.create.start.selectAction, ({ connection }) => ({
        params: {
          connectionId: connection.id!,
        },
        state: {
          connection,
        },
      })),
      configureAction: makeResolver<
        IEditorConfigureAction,
        IStartConfigurationPageRouteParams,
        IStartConfigurationPageRouteState
      >(
        routes.create.start.configureAction,
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
      ),
    },
    finish: {
      selectConnection: makeResolver<
        {
          integration: Integration;
          startConnection: ConnectionOverview;
          startAction: Action;
        },
        null,
        IFinishConnectionRouteState
      >(
        routes.create.finish.selectConnection,
        ({ integration, startConnection, startAction }) => ({
          params: null,
          state: {
            integration,
            startAction,
            startConnection,
          },
        })
      ),
      selectAction: makeResolver<
        {
          integration: Integration;
          startConnection: ConnectionOverview;
          startAction: Action;
          finishConnection: ConnectionOverview;
        },
        IFinishActionRouteParams,
        IFinishActionRouteState
      >(
        routes.create.finish.selectAction,
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
      ),
      configureAction: makeResolver<
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
        routes.create.finish.configureAction,
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
      ),
    },
    configure: {
      index: makeResolver<IEditorIndex, IBaseRouteParams, IBaseRouteState>(
        routes.create.configure.index,
        configureIndexMapper
      ),
      addStep: {
        selectConnection: makeResolver<
          IEditorSelectConnection,
          ISelectConnectionRouteParams,
          ISelectConnectionRouteState
        >(
          routes.create.configure.addStep.selectConnection,
          configureSelectConnectionMapper
        ),
        selectAction: makeResolver<
          IEditorSelectAction,
          ISelectActionRouteParams,
          ISelectActionRouteState
        >(
          routes.create.configure.addStep.selectAction,
          configureSelectActionMapper
        ),
        configureAction: makeResolver<
          IEditorConfigureAction,
          IConfigureActionRouteParams,
          IConfigureActionRouteState
        >(
          routes.create.configure.addStep.configureAction,
          configureConfigureActionMapper
        ),
      },
      editStep: {
        selectAction: makeResolver<
          IEditorSelectAction,
          ISelectActionRouteParams,
          ISelectActionRouteState
        >(
          routes.create.configure.editStep.selectAction,
          configureSelectActionMapper
        ),
        configureAction: makeResolver<
          IEditorConfigureAction,
          IConfigureActionRouteParams,
          IConfigureActionRouteState
        >(
          routes.create.configure.editStep.configureAction,
          configureConfigureActionMapper
        ),
      },
      saveAndPublish: makeResolver<
        { integration: Integration },
        null,
        ISaveIntegrationRouteState
      >(routes.create.configure.saveAndPublish, ({ integration }) => ({
        params: null,
        state: {
          integration,
        },
      })),
    },
  },
  integration: {
    activity: makeResolver<
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
    })),
    details: makeResolver<
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
    })),
    edit: {
      index: makeResolver<IEditorIndex, IBaseRouteParams, IBaseRouteState>(
        routes.integration.edit.index,
        configureIndexMapper
      ),
      addStep: {
        selectConnection: makeResolver<
          IEditorSelectConnection,
          ISelectConnectionRouteParams,
          ISelectConnectionRouteState
        >(
          routes.integration.edit.addStep.selectConnection,
          configureSelectConnectionMapper
        ),
        selectAction: makeResolver<
          IEditorSelectAction,
          ISelectActionRouteParams,
          ISelectConnectionRouteState
        >(
          routes.integration.edit.addStep.selectAction,
          configureSelectActionMapper
        ),
        configureAction: makeResolver<
          IEditorConfigureAction,
          IConfigureActionRouteParams,
          IConfigureActionRouteState
        >(
          routes.integration.edit.addStep.configureAction,
          configureConfigureActionMapper
        ),
      },
      editStep: {
        selectAction: makeResolver<
          IEditorSelectAction,
          ISelectActionRouteParams,
          ISelectActionRouteState
        >(
          routes.integration.edit.editStep.selectAction,
          configureSelectActionMapper
        ),
        configureAction: makeResolver<
          IEditorConfigureAction,
          IConfigureActionRouteParams,
          IConfigureActionRouteState
        >(
          routes.integration.edit.editStep.configureAction,
          configureConfigureActionMapper
        ),
      },
      saveAndPublish: makeResolver<
        IEditorIndex,
        ISaveIntegrationRouteParams,
        ISaveIntegrationRouteState
      >(routes.integration.edit.saveAndPublish, configureIndexMapper),
    },
    metrics: makeResolver<
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
    })),
  },
};
