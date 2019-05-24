import { reverse } from 'named-urls';

export interface IRoute<P, S> {
  params?: P;
  state?: S;
  route?: string;
}

export interface IResolvedRoute<P, S> extends IRoute<P, S> {
  pathname: string;
}

/**
 * Creates a function that takes a route and some `data` `T` that returns the
 * reversed URL.
 * Use `mapper` to write the business logic required to convert the `data` object
 * to the basic params that can be passed in an url (strings and numbers), and to
 * set the state object that will be pushed in the history together with the url.
 * @param defaultRoute
 * @param mapper
 */
export function makeResolver<T, P = any, S = any>(
  defaultRoute: string,
  mapper: (data: T) => IRoute<P, S>
) {
  return (data: T): IResolvedRoute<P, S> => {
    const { params, route, state } = mapper(data);
    return {
      params,
      pathname: reverse(route || defaultRoute, params || {}),
      route: route || defaultRoute,
      state,
    };
  };
}

/**
 * Creates a function that takes a route and some `data` `T` that returns the
 * reversed URL.
 * Use `mapper` to write the business logic required to convert the `data` object
 * to the basic params that can be passed in an url (strings and numbers), and to
 * set the state object that will be pushed in the history together with the url.
 * @param defaultRoute
 * @param mapper
 */
export function makeResolverNoParamsWithDefaults<P = any, S = any>(
  defaultRoute: string,
  mapper: () => IRoute<P, S>
) {
  return (): IResolvedRoute<P, S> => {
    const { params, route, state } = mapper();
    return {
      params,
      pathname: reverse(route || defaultRoute, params || {}),
      route: route || defaultRoute,
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
  return (): IResolvedRoute<any, any> => ({
    pathname: reverse(route),
    route,
  });
}
