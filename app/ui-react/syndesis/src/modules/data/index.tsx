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
import { ViewEditorSqlPage } from './pages/viewEditor';
import routes from './routes';
import { ViewCreateApp } from './ViewCreateApp';
import { ViewsImportApp } from './ViewsImportApp';

export class DataModule extends React.Component {
  public render() {
    return (
      <>
        <Switch>
          <Redirect
            path={routes.root}
            exact={true}
            to={routes.virtualizations.list}
          />
          <Route
            path={routes.virtualizations.virtualization.views.importSource.root}
            component={ViewsImportApp}
          />
          <Route
            path={routes.virtualizations.virtualization.views.createView.root}
            component={ViewCreateApp}
          />
          <Route
            path={routes.virtualizations.virtualization.views.edit.sql}
            exact={true}
            component={ViewEditorSqlPage}
          />
          <Route
            path={routes.virtualizations.create}
            exact={true}
            component={VirtualizationCreatePage}
          />
          <Route
            path={routes.virtualizations.list}
            exact={true}
            component={VirtualizationsPage}
          />
          <Redirect
            path={routes.virtualizations.virtualization.root}
            exact={true}
            to={routes.virtualizations.virtualization.views.root}
          />
          <Route
            path={routes.virtualizations.virtualization.views.root}
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
      </>
    );
  }
}
