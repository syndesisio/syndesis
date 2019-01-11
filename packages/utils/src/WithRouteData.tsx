import * as React from 'react';
import { RouteComponentProps } from 'react-router';
import { WithRouter } from './WithRouter';

export interface IWithRouteDataProps<P, S> {
  children(params: P, state: S, route: RouteComponentProps): any;
}

export class WithRouteData<P, S> extends React.Component<
  IWithRouteDataProps<P, S>
> {
  public render() {
    return (
      <WithRouter>
        {route => {
          const params: P = route.match.params as P;
          const state: S = route.location.state as S;
          return this.props.children(params, state, route);
        }}
      </WithRouter>
    );
  }
}
