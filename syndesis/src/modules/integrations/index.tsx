import * as React from 'react';
import { Route, Switch } from 'react-router';
import { IntegrationCreatorApp } from './IntegrationCreatorApp';
import { IntegrationEditorApp } from './IntegrationEditorApp';
import { IntegrationsPage, TestAtlasmapPage } from './pages';
import routes from './routes';

export class IntegrationsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route path={routes.create.root} component={IntegrationCreatorApp} />
        <Route
          path={routes.integration.edit.root}
          component={IntegrationEditorApp}
        />
        <Route path={routes.list} exact={true} component={IntegrationsPage} />
        <Route
          path={'/integrations/atlasmap'}
          exact={true}
          component={TestAtlasmapPage}
        />
      </Switch>
    );
  }
}
