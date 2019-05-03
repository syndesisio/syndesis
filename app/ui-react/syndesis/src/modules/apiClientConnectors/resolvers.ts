/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Connector } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import routes from './routes';

export default {
  apiConnector: makeResolver<{ apiConnector: Connector }>(
    routes.apiConnector,
    ({ apiConnector }) => ({
      params: {
        apiConnectorId: apiConnector.id,
      },
      state: {
        apiConnector,
      },
    })
  ),
  create: {
    upload: makeResolverNoParams(routes.create.upload),
    review: makeResolverNoParams(routes.create.review),
    security: makeResolverNoParams(routes.create.security),
    save: makeResolverNoParams(routes.create.save),
  },
  list: makeResolverNoParams(routes.list),
};
