import * as React from 'react';
import { Route, Switch } from 'react-router';
import {
  IntegrationCreatorSelectConnectionActionConfigurationPage,
  IntegrationCreatorSelectConnectionActionsPage,
  IntegrationCreatorSelectConnectionPage,
} from './pages';

export interface IIntegrationCreatorAppProps {
  baseurl: string;
}

export default class IntegrationCreatorApp extends React.Component<
  IIntegrationCreatorAppProps
> {
  public render() {
    return (
      <Switch>
        <Route
          path={`${this.props.baseurl}/:connectionId/:actionId/:step?`}
          exact={true}
          component={IntegrationCreatorSelectConnectionActionConfigurationPage}
        />
        <Route
          path={`${this.props.baseurl}/:connectionId`}
          exact={true}
          component={IntegrationCreatorSelectConnectionActionsPage}
        />
        <Route
          path={this.props.baseurl}
          exact={true}
          component={IntegrationCreatorSelectConnectionPage}
        />
      </Switch>
    );
  }
}
