import * as React from 'react';
import { Route, Switch } from 'react-router';
import { IntegrationsPage, TestAtlasmapPage } from './pages';

export interface IIntegrationsAppProps {
  baseurl: string;
}

export default class IntegrationsApp extends React.Component<
  IIntegrationsAppProps
> {
  public render() {
    return (
      <Switch>
        <Route
          path={this.props.baseurl}
          exact={true}
          component={IntegrationsPage}
        />
        <Route
          path={`${this.props.baseurl}/atlasmap`}
          exact={true}
          component={TestAtlasmapPage}
        />
      </Switch>
    );
  }
}
