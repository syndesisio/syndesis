import * as React from 'react';
import { Route, Switch } from 'react-router';
import ApiConnectorsPage from './pages/ApiConnectorsPage';
import ExtensionsPage from './pages/ExtensionsPage';

export interface ICustomizationsAppProps {
  baseurl: string;
}

export default class CustomizationApp extends React.Component<
  ICustomizationsAppProps
> {
  public render() {
    return (
      <Switch>
        <Route
          path={this.props.baseurl}
          exact={true}
          component={ApiConnectorsPage}
        />
        <Route
          path={`${this.props.baseurl}/api-connector`}
          exact={true}
          component={ApiConnectorsPage}
        />
        <Route
          path={`${this.props.baseurl}/extensions`}
          exact={true}
          component={ExtensionsPage}
        />
      </Switch>
    );
  }
}
