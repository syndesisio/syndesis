/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { getStep } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import routes from './routes';

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
      configureAction: makeResolver<{
        connection: ConnectionOverview;
        actionId: string;
        step?: number;
        integration?: Integration;
        updatedIntegration?: Integration;
      }>(
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
      index: makeResolver<{ integration: Integration }>(
        routes.create.configure.index,
        ({ integration }) => ({
          state: {
            integration,
          },
        })
      ),
      addStep: {
        selectConnection: makeResolver<{
          position: string;
          integration: Integration;
        }>(
          routes.create.configure.addStep.selectConnection,
          ({ position, integration }) => ({
            params: {
              position,
            },
            state: {
              integration,
            },
          })
        ),
        selectAction: makeResolver<{
          position: string;
          integration: Integration;
          connection: ConnectionOverview;
        }>(
          routes.create.configure.addStep.selectAction,
          ({ connection, position, integration }) => ({
            params: {
              position,
              connectionId: connection.id,
            },
            state: {
              integration,
              connection,
            },
          })
        ),
        configureAction: makeResolver<{
          connection: ConnectionOverview;
          actionId: string;
          step?: number;
          integration?: Integration;
          updatedIntegration?: Integration;
          position: string;
        }>(
          routes.create.configure.addStep.configureAction,
          ({
            connection,
            integration,
            actionId,
            step,
            position,
            updatedIntegration,
          }) => ({
            params: {
              connectionId: connection.id,
              actionId,
              step,
              position,
            },
            state: {
              connection,
              integration,
              updatedIntegration,
            },
          })
        ),
      },
      editStep: {
        selectAction: makeResolver<{
          position: string;
          integration: Integration;
          connection: ConnectionOverview;
        }>(
          routes.create.configure.editStep.selectAction,
          ({ connection, position, integration }) => ({
            params: {
              position,
              connectionId: connection.id,
            },
            state: {
              integration,
              connection,
            },
          })
        ),
        configureAction: makeResolver<{
          actionId: string;
          step?: number;
          integration: Integration;
          updatedIntegration?: Integration;
          position: string;
        }>(
          routes.create.configure.editStep.configureAction,
          ({ integration, actionId, step, position, updatedIntegration }) => {
            const positionAsNumber = parseInt(position, 10);
            const stepObject = getStep(integration, 0, positionAsNumber);
            return {
              params: {
                actionId,
                step,
                position,
                connectionId: stepObject.connection!.id!,
              },
              state: {
                integration,
                updatedIntegration,
                connection: stepObject.connection!,
                configuredProperties: stepObject.configuredProperties,
              },
            };
          }
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
      index: makeResolver<{ integration: Integration }>(
        routes.integration.edit.index,
        ({ integration }) => ({
          params: {
            integrationId: integration.id,
          },
          state: {
            integration,
          },
        })
      ),
      addStep: {
        selectConnection: makeResolver<{
          position: string;
          integration: Integration;
        }>(
          routes.integration.edit.addStep.selectConnection,
          ({ position, integration }) => ({
            params: {
              integrationId: integration.id,
              position,
            },
            state: {
              integration,
            },
          })
        ),
        selectAction: makeResolver<{
          position: string;
          integration: Integration;
          connection: ConnectionOverview;
        }>(
          routes.integration.edit.addStep.selectAction,
          ({ connection, position, integration }) => ({
            params: {
              integrationId: integration.id,
              position,
              connectionId: connection.id,
            },
            state: {
              integration,
              connection,
            },
          })
        ),
        configureAction: makeResolver<{
          connection: ConnectionOverview;
          actionId: string;
          step?: number;
          integration: Integration;
          updatedIntegration?: Integration;
          position: string;
        }>(
          routes.integration.edit.addStep.configureAction,
          ({
            connection,
            integration,
            actionId,
            step,
            position,
            updatedIntegration,
          }) => ({
            params: {
              integrationId: integration.id,
              connectionId: connection.id,
              actionId,
              step,
              position,
            },
            state: {
              connection,
              integration,
              updatedIntegration,
            },
          })
        ),
      },
      editStep: {
        selectAction: makeResolver<{
          position: string;
          integration: Integration;
          connection: ConnectionOverview;
        }>(
          routes.integration.edit.editStep.selectAction,
          ({ connection, position, integration }) => ({
            params: {
              integrationId: integration.id,
              position,
              connectionId: connection.id,
            },
            state: {
              integration,
              connection,
            },
          })
        ),
        configureAction: makeResolver<{
          actionId: string;
          step?: number;
          integration: Integration;
          updatedIntegration?: Integration;
          position: string;
        }>(
          routes.integration.edit.editStep.configureAction,
          ({ integration, actionId, step, position, updatedIntegration }) => {
            const positionAsNumber = parseInt(position, 10);
            const stepObject = getStep(integration, 0, positionAsNumber);
            return {
              params: {
                integrationId: integration.id,
                actionId,
                step,
                position,
                connectionId: stepObject.connection!.id!,
              },
              state: {
                integration,
                updatedIntegration,
                connection: stepObject.connection,
                configuredProperties: stepObject.configuredProperties || {},
              },
            };
          }
        ),
      },
      saveAndPublish: makeResolver<{ integration: Integration }>(
        routes.integration.edit.saveAndPublish,
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
