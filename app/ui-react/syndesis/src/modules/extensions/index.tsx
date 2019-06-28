import * as React from 'react';
import { Route, Switch } from 'react-router';
import { ExtensionDetailsPage } from './pages/ExtensionDetailsPage';
import { ExtensionImportPage } from './pages/ExtensionImportPage';
import { ExtensionsPage } from './pages/ExtensionsPage';
import routes from './routes';

export class ExtensionsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route path={routes.list} exact={true} component={ExtensionsPage} />
        <Route
          path={routes.import}
          exact={true}
          component={ExtensionImportPage}
        />
        <Route
          path={routes.extension.details}
          exact={true}
          component={ExtensionDetailsPage}
        />
        <Route
          path={routes.extension.update}
          exact={true}
          component={ExtensionImportPage}
        />
      </Switch>
    );
  }
}
