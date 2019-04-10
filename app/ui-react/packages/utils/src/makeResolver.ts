import { reverse } from 'named-urls';

export interface IRoute {
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
 */
export function makeResolver<T>(route: string, mapper: (data: T) => IRoute) {
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
export function makeResolverNoParams(route: string) {
  return () => reverse(route);
}
