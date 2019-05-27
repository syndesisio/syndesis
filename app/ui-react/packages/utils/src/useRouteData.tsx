import useReactRouter from 'use-react-router';

export function useRouteData<P, S>() {
  const { location, match, history } = useReactRouter<P, S>();

  return {
    history,
    location,
    match,
    params: match.params,
    state: location.state,
  };
}
