import * as React from 'react';
import { Route, RouteComponentProps } from 'react-router';

export interface IWithRouteDataProps<P, S> {
  children(params: P, state: S, route: RouteComponentProps): any;
}

export class WithRouteData<P, S> extends React.Component<
  IWithRouteDataProps<P, S>
> {
  public render() {
    return (
      <Route>
        {route => {
          const params: P = route.match.params as P;
          const state: S = route.location.state as S;
          return this.props.children(params, state, route);
        }}
      </Route>
    );
  }
}
