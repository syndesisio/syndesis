import { WithRouter } from '@syndesis/utils';
import * as React from 'react';
import Loadable from 'react-loadable';
import { ModuleLoader } from '../../containers';

const LoadableCustomizationsPage = Loadable({
  loader: () =>
    import(/* webpackChunkName: "Customizations" */ './CustomizationsApp'),
  loading: ModuleLoader,
});

export class CustomizationsModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => <LoadableCustomizationsPage baseurl={match.url} />}
      </WithRouter>
    );
  }
}
