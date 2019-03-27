import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import ApiConnectorsPage from './pages/ApiConnectorsPage';
import ExtensionDetailsPage from './pages/ExtensionDetailsPage';
import ExtensionImportPage from './pages/ExtensionImportPage';
import ExtensionsPage from './pages/ExtensionsPage';
import routes from './routes';

export default class CustomizationApp extends React.Component {
  public render() {
    return (
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
          path={routes.extensions.import}
          exact={true}
          component={ExtensionImportPage}
        />
        // **This route must appear after import page**
        <Route
          path={routes.extensions.extension}
          exact={true}
          component={ExtensionDetailsPage}
        />
      </Switch>
    );
  }
}
