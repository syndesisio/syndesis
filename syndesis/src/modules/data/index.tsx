import * as React from 'react';
import Loadable from 'react-loadable';
import { ModuleLoader } from '../../containers';

const LoadableDataApp = Loadable({
  loader: () => import(/* webpackChunkName: "Virtualizations" */ './DataApp'),
  loading: ModuleLoader,
});

export class DataModule extends React.Component {
  public render() {
    return <LoadableDataApp />;
  }
}
