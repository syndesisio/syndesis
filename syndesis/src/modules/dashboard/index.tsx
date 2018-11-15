import { ModuleLoader } from '@syndesis/ui';
import { WithRouter } from '@syndesis/utils';
import * as React from 'react';
import Loadable from 'react-loadable';

const LoadableDashboardPage = Loadable({
  loader: () => import(/* webpackChunkName: "Dashboard" */ './DashboardApp'),
  loading: ModuleLoader,
});

export class DashboardModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => <LoadableDashboardPage baseurl={match.url} />}
      </WithRouter>
    );
  }
}
