import * as React from 'react';
import * as Loadable from 'react-loadable';
import { Route, Switch } from 'react-router';
import { ModuleLoader } from '../../components/ui';
import { WithRouter } from '../../containers';

const LoadableConnectionsPage = Loadable({
  loader: () => import(/* webpackChunkName: "ConnectionsPageChunk" */ './pages/ConnectionsPage'),
  loading: ModuleLoader
});

const LoadableNewConnectionPage = Loadable({
  loader: () => import(/* webpackChunkName: "NewConnectionPageChunk" */ './pages/NewConnectionPage'),
  loading: ModuleLoader
});

export class ConnectionsModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({match}) =>
          <Switch>
            <Route path={match.url} exact={true} component={LoadableConnectionsPage}/>
            <Route path={`${match.url}/new`} exact={true} component={LoadableNewConnectionPage}/>
          </Switch>
        }
      </WithRouter>
    )
  }
}