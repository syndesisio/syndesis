import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import VirtualizationCreatePage from './pages/VirtualizationCreatePage';
import VirtualizationsPage from './pages/VirtualizationsPage';
import routes from './routes';

export default class DataApp extends React.Component {
  public render() {
    return (
      <Switch>
        <Redirect
          path={routes.root}
          exact={true}
          to={routes.virtualizations.list}
        />
        <Route
          path={routes.virtualizations.list}
          exact={true}
          component={VirtualizationsPage}
        />
        <Route
          path={routes.virtualizations.create}
          exact={true}
          component={VirtualizationCreatePage}
        />
      </Switch>
    );
  }
}
