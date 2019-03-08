/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Connector, Extension } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import routes from './routes';

export default {
  apiConnectors: {
    apiConnector: makeResolver<{ apiConnector: Connector }>(
      routes.apiConnectors.apiConnector,
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
      upload: makeResolverNoParams(routes.apiConnectors.create.upload),
      review: makeResolverNoParams(routes.apiConnectors.create.review),
      security: makeResolverNoParams(routes.apiConnectors.create.security),
      save: makeResolverNoParams(routes.apiConnectors.create.save),
    },
    list: makeResolverNoParams(routes.apiConnectors.list),
  },
  extensions: {
    extension: makeResolver<{ extension: Extension }>(
      routes.extensions.extension,
      ({ extension }) => ({
        params: {
          extensionId: extension.id,
        },
        state: {
          extension,
        },
      })
    ),
    import: makeResolverNoParams(routes.extensions.import),
    list: makeResolverNoParams(routes.extensions.list),
  },
  root: makeResolverNoParams(routes.apiConnectors.list),
};
