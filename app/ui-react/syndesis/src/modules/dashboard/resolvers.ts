/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { reverse } from 'named-urls';
import routes from './routes';

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
  root: makeResolverNoParams(routes.root),
};
