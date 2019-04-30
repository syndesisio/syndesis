import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import { OAuthAppsPage } from './pages/OAuthAppsPage';
import routes from './routes';

export class SettingsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Redirect path={routes.root} exact={true} to={routes.oauthApps.root} />
        <Route
          path={routes.oauthApps.root}
          exact={true}
          component={OAuthAppsPage}
        />
      </Switch>
    );
  }
}
