/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Connection, Connector } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import routes from './routes';

export default {
  connections: makeResolverNoParams(routes.connections),
  connection: {
    details: makeResolver<{ connection: Connection }>(
      routes.connection.details,
      ({ connection }) => ({
        params: {
          connectionId: connection.id,
        },
        state: {
          connection,
        },
      })
    ),
    edit: makeResolver<{ connection: Connection }>(
      routes.connection.edit,
      ({ connection }) => ({
        params: {
          connectionId: connection.id,
        },
        state: {
          connection,
        },
      })
    ),
  },
  create: {
    selectConnector: makeResolverNoParams(routes.create.selectConnector),
    configureConnector: makeResolver<{
      connector: Connector;
    }>(routes.create.configureConnector, ({ connector }) => ({
      params: {
        connectorId: connector.id,
      },
      state: {
        connector,
      },
    })),
    review: makeResolver<{
      connector: Connector;
      configuredProperties: { [key: string]: string };
    }>(routes.create.review, ({ connector, configuredProperties }) => ({
      state: {
        connector,
        configuredProperties,
      },
    })),
  },
};
