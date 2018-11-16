import { WithRouter } from '@syndesis/utils';
import * as React from 'react';
import Loadable from 'react-loadable';
import { ModuleLoader } from '../../containers';

const LoadableIntegrationsPage = Loadable({
  loader: () =>
    import(/* webpackChunkName: "Integrations" */ './IntegrationsApp'),
  loading: ModuleLoader,
});

export class IntegrationsModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => <LoadableIntegrationsPage baseurl={match.url} />}
      </WithRouter>
    );
  }
}
