import { Integration } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Route, Switch } from 'react-router';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../containers';
import { integration as integrationPages } from './pages';
import resolvers from './resolvers';
import routes from './routes';

export interface IIntegrationEditorAppRouteState {
  integration: Integration;
}

/**
 * Entry point for the integration editor app. This is shown when an user clicks
 * on the "Edit" button for any existing integration.
 *
 * Since all the creation routes will show the same breadcrumb and require the
 * left navigation bar to be closed to reclaim space, we do it here.
 *
 * Almost all of the routes *require* some state to be passed for them to
 * properly work, so an url that works for an user *will not work* for another.
 * If you try and open the same url on a different browser, the code will throw
 * an exception because of this.
 *
 * We should set up an error boundary[1] to catch these errors and tell the user
 * that he reached an invalid url, or redirect him to a safe page.
 *
 * [1] https://reactjs.org/docs/error-boundaries.html
 *
 * @todo add an error handler!
 * @todo i18n everywhere!
 */
export class IntegrationEditorApp extends React.Component {
  public render() {
    return (
      <WithRouteData<null, IIntegrationEditorAppRouteState>>
        {(_, { integration }) => (
          <WithClosedNavigation>
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
                component={integrationPages.edit.main.AddStepPage}
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
                component={integrationPages.edit.main.SaveIntegrationPage}
              />
            </Switch>
          </WithClosedNavigation>
        )}
      </WithRouteData>
    );
  }
}
