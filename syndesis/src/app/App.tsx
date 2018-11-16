import { ApiContext } from '@syndesis/api';
import { PfVerticalNavItem } from '@syndesis/ui';
import * as React from 'react';
import { Route, Switch } from 'react-router-dom';
import './App.css';
import { ConnectionsModule } from '../modules/connections';
import { DashboardModule } from '../modules/dashboard';
import { IntegrationsModule } from '../modules/integrations';
import { AppContext } from './AppContext';
import { Layout } from './Layout';
import { WithConfig } from './WithConfig';

interface IAppProps {}

interface IAppState {}

export class App extends React.Component<IAppProps, IAppState> {
  public render() {
    return (
      <WithConfig>
        {config => (
          <AppContext.Provider
            value={{
              config: config,
              logout: this.logout,
            }}
          >
            <ApiContext.Provider
              value={{
                apiUri: `${config.apiBase}${config.apiEndpoint}`,
              }}
            >
              <Layout navbar={this.renderNavbar()}>
                <React.Fragment>
                  <Switch>
                    <Route path="/" exact={true} component={DashboardModule} />
                    <Route
                      path="/integrations"
                      component={IntegrationsModule}
                    />
                    <Route path="/connections" component={ConnectionsModule} />
                  </Switch>
                </React.Fragment>
              </Layout>
            </ApiContext.Provider>
          </AppContext.Provider>
        )}
      </WithConfig>
    );
  }

  public renderNavbar() {
    return [
      <PfVerticalNavItem
        icon={'fa fa-tachometer'}
        to={'/'}
        exact={true}
        label={'Home'}
        key={1}
      />,
      <PfVerticalNavItem
        icon={'pficon pficon-integration'}
        to={'/integrations'}
        label={'Integrations'}
        key={2}
      />,
      <PfVerticalNavItem
        icon={'pficon pficon-plugged'}
        to={'/connections'}
        label={'Connections'}
        key={3}
      />,
      <PfVerticalNavItem
        icon={'fa fa-cube'}
        to={'/customizations'}
        label={'Customizations'}
        key={4}
      />,
      <PfVerticalNavItem
        icon={'pficon pficon-settings'}
        to={'/settings'}
        label={'Settings'}
        key={5}
      />,
    ];
  }

  public logout = () => {
    // do nothing
  };
}
