import * as React from 'react';
import * as Loadable from 'react-loadable';
import { Route, Switch } from 'react-router';
import { WithRouter } from '../../containers';
import { ModuleLoader } from '../../ui';

const LoadableDashboardPage = Loadable({
  loader: () => import(/* webpackChunkName: "DashboardPageChunk" */'./pages/DashboardPage'),
  loading: ModuleLoader
});

export class DashboardModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({match}) =>
          <Switch>
            <Route path={match.url} exact={true} component={LoadableDashboardPage}/>
          </Switch>
        }
      </WithRouter>
    )
  }
}