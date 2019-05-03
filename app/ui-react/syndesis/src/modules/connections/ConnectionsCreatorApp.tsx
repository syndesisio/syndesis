import { Breadcrumb, PageSection } from '@syndesis/ui';
import * as React from 'react';
import { Route, Switch } from 'react-router';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../shared';
import { create } from './pages';
import resolvers from './resolvers';
import routes from './routes';

export default class ConnectionsCreatorApp extends React.Component {
  public render() {
    return (
      <PageSection noPadding={true}>
        <WithClosedNavigation>
          <Breadcrumb>
            <Link to={resolvers.connections()}>Connections</Link>
            <span>Create connection</span>
          </Breadcrumb>
          <Switch>
            <Route
              path={routes.create.selectConnector}
              exact={true}
              component={create.ConnectorsPage}
            />
            <Route
              path={routes.create.configureConnector}
              exact={true}
              component={create.ConfigurationPage}
            />
            <Route
              path={routes.create.review}
              exact={true}
              component={create.ReviewPage}
            />
          </Switch>
        </WithClosedNavigation>
      </PageSection>
    );
  }
}
