import { ModuleLoader } from '@syndesis/app/components';
import { WithRouter } from '@syndesis/app/containers';
import * as React from 'react';
import Loadable from 'react-loadable';
import { Route, Switch } from 'react-router';

const LoadableDashboardPage = Loadable({
  loader: () =>
    import(/* webpackChunkName: "DashboardPageChunk" */ './pages/DashboardPage'),
  loading: ModuleLoader
});

export class DashboardModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({match}) => (
          <Switch>
            <Route
              path={match.url}
              exact={true}
              component={LoadableDashboardPage}
            />
          </Switch>
        )}
      </WithRouter>
    );
  }
}
