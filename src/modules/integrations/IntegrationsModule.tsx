import * as React from 'react';
import * as Loadable from 'react-loadable';
import { Route, Switch } from 'react-router';
import { WithRouter } from '../../containers';
import { ModuleLoader } from '../../ui';

const LoadableIntegrationsPage = Loadable({
  loader: () => import('./pages/IntegrationsPage'),
  loading: ModuleLoader
});

export class IntegrationsModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({match}) =>
          <Switch>
            <Route path={match.url} exact={true} component={LoadableIntegrationsPage}/>
          </Switch>
        }
      </WithRouter>
    )
  }
}