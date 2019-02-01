/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import { reverse } from 'named-urls';
import routes from './routes';

interface IRoute {
  params?: any;
  state?: any;
}

// TODO: perhaps move these 2 helper functions in the @syndesis/utils package?

/**
 * Creates a function that takes a route and some `data` `T` that returns the
 * reversed URL.
 * Use `mapper` to write the business logic required to convert the `data` object
 * to the basic params that can be passed in an url (strings and numbers), and to
 * set the state object that will be pushed in the history together with the url.
 * @param route
 * @param mapper
 */
function makeResolver<T>(route: string, mapper: (data: T) => IRoute) {
  return (data: T) => {
    const { params, state } = mapper(data);
    return {
      pathname: reverse(route, params),
      state,
    };
  };
}

/**
 * Creates a function that takes a route and some `data` `T` that returns the
 * reversed URL.
 * @param route
 */
function makeResolverNoParams(route: string) {
  return () => reverse(route);
}

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
      addConnection: {
        selectConnection: makeResolver<{
          position: string;
          integration: Integration;
        }>(
          routes.create.configure.addConnection.selectConnection,
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
          routes.create.configure.addConnection.selectAction,
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
          routes.create.configure.addConnection.configureAction,
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
      editConnection: {
        selectAction: makeResolver<{
          position: string;
          integration: Integration;
          connection: ConnectionOverview;
        }>(
          routes.create.configure.editConnection.selectAction,
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
          routes.create.configure.editConnection.configureAction,
          ({ integration, actionId, step, position, updatedIntegration }) => ({
            params: {
              actionId,
              step,
              position,
            },
            state: {
              integration,
              updatedIntegration,
            },
          })
        ),
      },
      addStep: {
        selectStep: makeResolver<{
          position: string;
          integration: Integration;
        }>(
          routes.create.configure.addStep.selectStep,
          ({ position, integration }) => ({
            params: {
              position,
            },
            state: {
              integration,
            },
          })
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
      addConnection: {
        selectConnection: makeResolver<{
          position: string;
          integration: Integration;
        }>(
          routes.integration.edit.addConnection.selectConnection,
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
          routes.integration.edit.addConnection.selectAction,
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
          routes.integration.edit.addConnection.configureAction,
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
      editConnection: {
        selectAction: makeResolver<{
          position: string;
          integration: Integration;
          connection: ConnectionOverview;
        }>(
          routes.integration.edit.editConnection.selectAction,
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
          routes.integration.edit.editConnection.configureAction,
          ({ integration, actionId, step, position, updatedIntegration }) => ({
            params: {
              integrationId: integration.id,
              actionId,
              step,
              position,
            },
            state: {
              integration,
              updatedIntegration,
            },
          })
        ),
      },
      addStep: {
        selectStep: makeResolver<{
          position: string;
          integration: Integration;
        }>(
          routes.integration.edit.addStep.selectStep,
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
  },
};
