import { Integration } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Route, Switch } from 'react-router';
import { Link } from 'react-router-dom';
import { integration as integrationPages } from './pages';
import resolvers from './resolvers';
import routes from './routes';

export interface IIntegrationEditorAppRouteState {
  integration: Integration;
}

export class IntegrationEditorApp extends React.Component {
  public render() {
    return (
      <WithRouteData<null, IIntegrationEditorAppRouteState>>
        {(_, { integration }, { history }) => (
          <>
            <Breadcrumb>
              <Link to={resolvers.list()}>Integrations</Link>
              <Link to={resolvers.integration.details({ integration })}>
                {integration.name}
              </Link>
              <span>Add to integration</span>
            </Breadcrumb>
            <Switch>
              <Route
                path={routes.integration.edit.index}
                exact={true}
                component={integrationPages.edit.AddStepPage}
              />
              <Route
                path={routes.integration.edit.addConnection.selectConnection}
                exact={true}
                component={
                  integrationPages.edit.addConnection.SelectConnectionPage
                }
              />
              <Route
                path={routes.integration.edit.addConnection.selectAction}
                exact={true}
                component={integrationPages.edit.addConnection.SelectActionPage}
              />
              <Route
                path={routes.integration.edit.addConnection.configureAction}
                exact={true}
                component={
                  integrationPages.edit.addConnection.ConfigureActionPage
                }
              />
              <Route
                path={routes.integration.edit.editConnection.selectAction}
                exact={true}
                component={
                  integrationPages.edit.editConnection.SelectActionPage
                }
              />
              <Route
                path={routes.integration.edit.editConnection.configureAction}
                exact={true}
                component={
                  integrationPages.edit.editConnection.ConfigureActionPage
                }
              />
              <Route
                path={routes.integration.edit.saveAndPublish}
                exact={true}
                component={integrationPages.edit.SaveIntegrationPage}
              />
            </Switch>
          </>
        )}
      </WithRouteData>
    );
  }
}
