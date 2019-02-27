import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import { Redirect, Route, Switch } from 'react-router';
import ApiConnectorsPage from './pages/ApiConnectorsPage';
import ExtensionDetailsPage from './pages/ExtensionDetailsPage';
import ExtensionsPage from './pages/ExtensionsPage';
import routes from './routes';

export interface ICustomizationsAppProps {
  baseurl: string;
}

export default class CustomizationApp extends React.Component<
  ICustomizationsAppProps
> {
  public render() {
    return (
      <NamespacesConsumer ns={['customizations', 'shared']}>
        {t => (
          <Switch>
            <Redirect
              path={routes.root}
              exact={true}
              to={routes.apiConnectors.list}
            />
            <Route
              path={routes.apiConnectors.list}
              exact={true}
              component={ApiConnectorsPage}
            />
            <Route
              path={routes.extensions.list}
              exact={true}
              component={ExtensionsPage}
            />
            <Route
              path={routes.extensions.extension}
              exact={true}
              component={ExtensionDetailsPage}
            />
          </Switch>
        )}
      </NamespacesConsumer>
    );
  }
}
