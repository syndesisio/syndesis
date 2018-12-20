import * as React from 'react';
import { Route, Switch } from 'react-router';
import {
  IntegrationCreatorSelectConnectionActionPage,
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
          path={`${this.props.baseurl}/:connectionId`}
          component={IntegrationCreatorSelectConnectionActionPage}
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
