import { WithRouter } from '@syndesis/utils';
import * as React from 'react';
import Loadable from 'react-loadable';
import { ModuleLoader } from '../../containers';

const LoadableIntegrationsApp = Loadable({
  loader: () =>
    import(/* webpackChunkName: "Integrations" */ './IntegrationsApp'),
  loading: ModuleLoader,
});

const LoadableIntegrationCreatorApp = Loadable({
  loader: () =>
    import(/* webpackChunkName: "IntegrationCreator" */ './IntegrationCreatorApp'),
  loading: ModuleLoader,
});

export class IntegrationsModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <>
            <LoadableIntegrationsApp baseurl={match.path} />
            <LoadableIntegrationCreatorApp baseurl={`${match.path}/create`} />
          </>
        )}
      </WithRouter>
    );
  }
}
