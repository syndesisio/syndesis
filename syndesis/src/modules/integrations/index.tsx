import * as React from 'react';
import { Route, Switch } from 'react-router';
import {
  IntegrationCreatorStartActionPage,
  IntegrationCreatorStartConfigurationPage,
  IntegrationCreatorStartConnectionPage,
  IntegrationsPage,
  TestAtlasmapPage,
} from './pages';
import routes from './routes';

export class IntegrationsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route
          path={routes.integrations.create.start.configureAction}
          exact={true}
          component={IntegrationCreatorStartConfigurationPage}
        />
        <Route
          path={routes.integrations.create.start.selectAction}
          exact={true}
          component={IntegrationCreatorStartActionPage}
        />
        <Route
          path={routes.integrations.create.begin}
          exact={true}
          component={IntegrationCreatorStartConnectionPage}
        />
        <Route
          path={routes.integrations.list}
          exact={true}
          component={IntegrationsPage}
        />
        <Route
          path={'/integrations/atlasmap'}
          exact={true}
          component={TestAtlasmapPage}
        />
      </Switch>
    );
  }
}
