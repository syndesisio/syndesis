/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Connection } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import {
  IConfigurationPageRouteParams,
  IConfigurationPageRouteState,
} from './pages/create/ConfigurationPage';
import {
  IReviewPageRouteParams,
  IReviewPageRouteState,
} from './pages/create/ReviewPage';
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
    configureConnector: makeResolver<
      IConfigurationPageRouteState,
      IConfigurationPageRouteParams,
      IConfigurationPageRouteState
    >(routes.create.configureConnector, ({ connector }) => ({
      params: {
        connectorId: connector.id!,
      },
      state: {
        connector,
      },
    })),
    review: makeResolver<
      IReviewPageRouteState,
      IReviewPageRouteParams,
      IReviewPageRouteState
    >(routes.create.review, ({ connector, configuredProperties }) => ({
      params: {
        connectorId: connector.id!,
      },
      state: {
        connector,
        configuredProperties,
      },
    })),
  },
};
