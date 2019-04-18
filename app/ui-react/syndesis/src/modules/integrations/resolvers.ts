/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { getStep } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
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
  },
  state: {
    integration,
  },
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
    },
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
      connectionId: connection.id,
    },
    state: {
      ...state,
      connection,
    },
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
      step,
    },
    state: {
      ...state,
      updatedIntegration,
      configuredProperties: stepObject.configuredProperties,
    },
  };
};

// TODO: unit test every single one of these resolvers 😫
export default {
  list: makeResolverNoParams(routes.list),
  create: {
    root: makeResolverNoParams(routes.create.root),
    start: {
      selectConnection: makeResolverNoParams(
        routes.create.start.selectConnection
      ),
      selectAction: makeResolver<{ connection: ConnectionOverview }>(
        routes.create.start.selectAction,
        ({ connection }) => ({
          params: {
            connectionId: connection.id,
          },
          state: {
            connection,
          },
        })
      ),
      configureAction: makeResolver<IEditorConfigureAction>(
        routes.create.start.configureAction,
        ({ connection, integration, actionId, step, updatedIntegration }) => ({
          params: {
            connectionId: connection.id,
            actionId,
            step,
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
      selectConnection: makeResolver<{
        integration: Integration;
        startConnection: ConnectionOverview;
        startAction: Action;
      }>(
        routes.create.finish.selectConnection,
        ({ integration, startConnection, startAction }) => ({
          state: {
            integration,
            startAction,
            startConnection,
          },
        })
      ),
      selectAction: makeResolver<{
        integration: Integration;
        startConnection: ConnectionOverview;
        startAction: Action;
        finishConnection: ConnectionOverview;
      }>(
        routes.create.finish.selectAction,
        ({ integration, startConnection, startAction, finishConnection }) => ({
          params: {
            connectionId: finishConnection.id,
          },
          state: {
            integration,
            startAction,
            startConnection,
            finishConnection,
          },
        })
      ),
      configureAction: makeResolver<{
        integration: Integration;
        updatedIntegration?: Integration;
        startConnection: ConnectionOverview;
        startAction: Action;
        finishConnection: ConnectionOverview;
        actionId: string;
        step?: number;
      }>(
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
            step,
            connectionId: finishConnection.id,
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
      index: makeResolver<IEditorIndex>(
        routes.create.configure.index,
        configureIndexMapper
      ),
      addStep: {
        selectConnection: makeResolver<IEditorSelectConnection>(
          routes.create.configure.addStep.selectConnection,
          configureSelectConnectionMapper
        ),
        selectAction: makeResolver<IEditorSelectAction>(
          routes.create.configure.addStep.selectAction,
          configureSelectActionMapper
        ),
        configureAction: makeResolver<IEditorConfigureAction>(
          routes.create.configure.addStep.configureAction,
          configureConfigureActionMapper
        ),
      },
      editStep: {
        selectAction: makeResolver<IEditorSelectAction>(
          routes.create.configure.editStep.selectAction,
          configureSelectActionMapper
        ),
        configureAction: makeResolver<IEditorConfigureAction>(
          routes.create.configure.editStep.configureAction,
          configureConfigureActionMapper
        ),
      },
      saveAndPublish: makeResolver<{ integration: Integration }>(
        routes.create.configure.saveAndPublish,
        ({ integration }) => ({
          state: {
            integration,
          },
        })
      ),
    },
  },
  integration: {
    activity: makeResolver<{ integration: Integration }>(
      routes.integration.activity,
      ({ integration }) => ({
        params: {
          integrationId: integration.id,
        },
        state: {
          integration,
        },
      })
    ),
    details: makeResolver<{ integration: Integration }>(
      routes.integration.details,
      ({ integration }) => ({
        params: {
          integrationId: integration.id,
        },
        state: {
          integration,
        },
      })
    ),
    edit: {
      index: makeResolver<IEditorIndex>(
        routes.integration.edit.index,
        configureIndexMapper
      ),
      addStep: {
        selectConnection: makeResolver<IEditorSelectConnection>(
          routes.integration.edit.addStep.selectConnection,
          configureSelectConnectionMapper
        ),
        selectAction: makeResolver<IEditorSelectAction>(
          routes.integration.edit.addStep.selectAction,
          configureSelectActionMapper
        ),
        configureAction: makeResolver<IEditorConfigureAction>(
          routes.integration.edit.addStep.configureAction,
          configureConfigureActionMapper
        ),
      },
      editStep: {
        selectAction: makeResolver<IEditorSelectAction>(
          routes.integration.edit.editStep.selectAction,
          configureSelectActionMapper
        ),
        configureAction: makeResolver<IEditorConfigureAction>(
          routes.integration.edit.editStep.configureAction,
          configureConfigureActionMapper
        ),
      },
      saveAndPublish: makeResolver<IEditorIndex>(
        routes.integration.edit.saveAndPublish,
        configureIndexMapper
      ),
    },
    metrics: makeResolver<{ integration: Integration }>(
      routes.integration.metrics,
      ({ integration }) => ({
        params: {
          integrationId: integration.id,
        },
        state: {
          integration,
        },
      })
    ),
  },
};
