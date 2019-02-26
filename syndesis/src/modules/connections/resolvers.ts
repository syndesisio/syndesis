/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Connector } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import routes from './routes';

export default {
  connections: makeResolverNoParams(routes.connections),
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
    review: makeResolverNoParams(routes.create.review),
  },
};
