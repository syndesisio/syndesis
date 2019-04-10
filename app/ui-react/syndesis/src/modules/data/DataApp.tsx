import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import {
  VirtualizationCreatePage,
  VirtualizationMetricsPage,
  VirtualizationRelationshipPage,
  VirtualizationsPage,
  VirtualizationSqlClientPage,
  VirtualizationViewsPage,
} from './pages';
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
        <Route
          path={routes.virtualizations.virtualization.views}
          exact={true}
          component={VirtualizationViewsPage}
        />
        <Route
          path={routes.virtualizations.virtualization.relationship}
          exact={true}
          component={VirtualizationRelationshipPage}
        />
        <Route
          path={routes.virtualizations.virtualization.sqlClient}
          exact={true}
          component={VirtualizationSqlClientPage}
        />
        <Route
          path={routes.virtualizations.virtualization.metrics}
          exact={true}
          component={VirtualizationMetricsPage}
        />
      </Switch>
    );
  }
}
