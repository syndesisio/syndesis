import useReactRouter from 'use-react-router';

export function useRouteData<P, S>() {
  const { location, match, history } = useReactRouter();

  return {
    history,
    location,
    match,
    params: (match.params || {}) as P,
    state: (location.state || {}) as S,
  };
}
