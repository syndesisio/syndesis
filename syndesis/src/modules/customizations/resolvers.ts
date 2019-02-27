/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Connector, Extension } from '@syndesis/models';
import { reverse } from 'named-urls';
import routes from './routes';

interface IRoute {
  params?: any;
  state?: any;
}

/**
 * Creates a function that takes a route and some `data` `T` that returns the
 * reversed URL.
 * Use `mapper` to write the business logic required to convert the `data` object
 * to the basic params that can be passed in an url (strings and numbers), and to
 * set the state object that will be pushed in the history together with the url.
 * @param route
 * @param mapper
 *
 * @todo perhaps move in the @syndesis/utils package?
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
 *
 * @todo perhaps move in the @syndesis/utils package?
 */
function makeResolverNoParams(route: string) {
  return () => reverse(route);
}

// TODO: unit test every single one of these resolvers 😫
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
