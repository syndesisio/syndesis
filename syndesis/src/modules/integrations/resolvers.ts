/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import { reverse } from 'named-urls';
import routes from './routes';

interface IRoute {
  params?: any;
  state?: any;
}

function makeResolver<T>(route: string, mapper: (data: T) => IRoute) {
  return (data: T) => {
    const { params, state } = mapper(data);
    return {
      pathname: reverse(route, params),
      state,
    };
  };
}
function makeResolverNoParams(route: string) {
  return () => reverse(route);
}

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
      }>(
        routes.create.start.configureAction,
        ({ connection, integration, actionId, step }) => ({
          params: {
            connectionId: connection.id,
            actionId,
            step,
          },
          state: {
            connection,
            integration,
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
        }) => ({
          params: {
            actionId,
            step,
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
          position: number;
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
      },
      addStep: {
        selectStep: makeResolver<{
          position: number;
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
    },
  },
};
