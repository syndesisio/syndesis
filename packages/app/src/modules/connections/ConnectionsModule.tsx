import { ModuleLoader } from '@syndesis/ui';
import * as React from 'react';
import Loadable from 'react-loadable';
import { Route, Switch } from 'react-router';
import { WithRouter } from 'src/containers/index';

const LoadableConnectionsPage = Loadable({
  loader: () =>
    import(/* webpackChunkName: "ConnectionsPageChunk" */ './pages/ConnectionsPage'),
  loading: ModuleLoader
});

const LoadableNewConnectionPage = Loadable({
  loader: () =>
    import(/* webpackChunkName: "NewConnectionPageChunk" */ './pages/NewConnectionPage'),
  loading: ModuleLoader
});

export class ConnectionsModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <Switch>
            <Route
              path={match.url}
              exact={true}
              component={LoadableConnectionsPage}
            />
            <Route
              path={`${match.url}/new`}
              exact={true}
              component={LoadableNewConnectionPage}
            />
          </Switch>
        )}
      </WithRouter>
    );
  }
}
