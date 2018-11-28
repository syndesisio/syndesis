import { ApiContext } from '@syndesis/api';
import { Loader, PfVerticalNavItem, UnrecoverableError } from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { withNamespaces, WithNamespaces } from 'react-i18next';
import { Route, Switch } from 'react-router-dom';
import { ConnectionsModule } from '../modules/connections';
import { DashboardModule } from '../modules/dashboard';
import { IntegrationsModule } from '../modules/integrations';
import './App.css';
import { AppContext } from './AppContext';
import { Layout } from './Layout';
import { WithConfig } from './WithConfig';

export class AppComponent extends React.Component<WithNamespaces> {
  public render() {
    return (
      <WithConfig>
        {({ config, loading, error }) => (
          <WithLoader
            loading={loading}
            error={error}
            loaderChildren={<Loader />}
            errorChildren={<UnrecoverableError />}
            minWait={1000}
          >
            {() => (
              <AppContext.Provider
                value={{
                  config: config!,
                  logout: this.logout,
                }}
              >
                <ApiContext.Provider
                  value={{
                    apiUri: `${config!.apiBase}${config!.apiEndpoint}`,
                  }}
                >
                  <Layout navbar={this.renderNavbar()}>
                    <React.Fragment>
                      <Switch>
                        <Route
                          path="/"
                          exact={true}
                          component={DashboardModule}
                        />
                        <Route
                          path="/integrations"
                          component={IntegrationsModule}
                        />
                        <Route
                          path="/connections"
                          component={ConnectionsModule}
                        />
                      </Switch>
                    </React.Fragment>
                  </Layout>
                </ApiContext.Provider>
              </AppContext.Provider>
            )}
          </WithLoader>
        )}
      </WithConfig>
    );
  }

  public renderNavbar() {
    const { t } = this.props;

    return [
      <PfVerticalNavItem
        icon={'fa fa-tachometer'}
        to={'/'}
        exact={true}
        label={t('Home')}
        key={1}
      />,
      <PfVerticalNavItem
        icon={'pficon pficon-integration'}
        to={'/integrations'}
        label={t('Integrations')}
        key={2}
      />,
      <PfVerticalNavItem
        icon={'pficon pficon-plugged'}
        to={'/connections'}
        label={t('Connections')}
        key={3}
      />,
      <PfVerticalNavItem
        icon={'fa fa-cube'}
        to={'/customizations'}
        label={t('Customizations')}
        key={4}
      />,
      <PfVerticalNavItem
        icon={'pficon pficon-settings'}
        to={'/settings'}
        label={t('Settings')}
        key={5}
      />,
    ];
  }

  public logout = () => {
    // do nothing
  };
}

export const App = withNamespaces(['shared'])(AppComponent);
