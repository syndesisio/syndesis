import { ModuleLoader } from "@syndesis/ui";
import { WithRouter } from "@syndesis/utils";
import * as React from "react";
import Loadable from 'react-loadable';

const LoadableConnectionsPage = Loadable({
  loader: () =>
    import(/* webpackChunkName: "Connections" */  "./ConnectionsApp"),
  loading: ModuleLoader
});

export class ConnectionsModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
      <LoadableConnectionsPage baseurl={match.url}/>
  )}
    </WithRouter>
  );
  }
}
