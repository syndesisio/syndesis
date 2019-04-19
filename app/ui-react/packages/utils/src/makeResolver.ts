import { reverse } from 'named-urls';

export interface IRoute<P, S> {
  params?: P;
  state?: S;
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
 * @param route
 * @param mapper
 */
export function makeResolver<T, P = any, S = any>(
  route: string,
  mapper: (data: T) => IRoute<P, S>
) {
  return (data: T): IResolvedRoute<P, S> => {
    const { params, state } = mapper(data);
    return {
      params,
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
