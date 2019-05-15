import * as React from 'react';
import { Route, Switch } from 'react-router';
import { IntegrationCreatorApp } from './IntegrationCreatorApp';
import { IntegrationEditorApp } from './IntegrationEditorApp';
import {
  ActivityPage,
  DetailsPage,
  ImportPage,
  IntegrationsPage,
  MetricsPage,
} from './pages';
import { ManageCiCdPage } from './pages/cicd';
import routes from './routes';

/**
 * Entry point for the integrations section.
 *
 * Since the section is logically split in sub-apps, each with their own routes,
 * we do the same thing here: instead of listing all the routes in a single place
 * we have components called "Apps" that will define their own route handlers.
 *
 * @todo the IntegrationCreatorApp and IntegrationEditorApp could benefit from
 * some refactoring aimed to DRY the business logic that's embedded in every
 * page of the same type (especially the configuration ones).
 * Actually the IntegrationEditorApp is the result of a copy-paste of this folder
 * `syndesis/src/modules/integrations/pages/create/configure/editStep`,
 * with updated titles and links between the steps.
 * This is an hint that at least those 2 "areas" could be DRYed up, but it could
 * be worth exploring options to take it up a notch and figure out a way to
 * simplify also the other pages.
 */
export class IntegrationsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route
          path={routes.manageCicd.root}
          exact={true}
          component={ManageCiCdPage}
        />
        <Route path={routes.import} exact={true} component={ImportPage} />
        <Route path={routes.create.root} component={IntegrationCreatorApp} />
        <Route
          path={routes.integration.edit.root}
          component={IntegrationEditorApp}
        />
        <Route path={routes.list} exact={true} component={IntegrationsPage} />
        <Route
          path={routes.integration.details}
          exact={true}
          component={DetailsPage}
        />
        <Route
          path={routes.integration.activity}
          exact={true}
          component={ActivityPage}
        />
        <Route
          path={routes.integration.metrics}
          exact={true}
          component={MetricsPage}
        />
      </Switch>
    );
  }
}
