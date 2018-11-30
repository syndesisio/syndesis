import { ApiContext } from '@syndesis/api';
import { Loader, PfVerticalNavItem, UnrecoverableError } from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Route, RouteComponentProps, Switch } from 'react-router-dom';
import './App.css';
import { AppContext } from './AppContext';
import { Layout } from './Layout';
import { WithConfig } from './WithConfig';

export interface IAppRoute {
  component:
    | React.ComponentType<RouteComponentProps<any>>
    | React.ComponentType<any>;
  exact?: boolean;
  icon: string;
  label: string;
  to: string;
}

export interface IAppBaseProps {
  routes: IAppRoute[];
}

export class App extends React.Component<IAppBaseProps> {
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
                      <Switch>{this.renderRoutes()}</Switch>
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
    return this.props.routes.map(({ exact, icon, label, to }, index) => (
      <PfVerticalNavItem
        exact={exact}
        icon={icon}
        label={label}
        to={to}
        key={index}
        data-testid={`navbar-link-${to}`}
      />
    ));
  }

  public renderRoutes() {
    return this.props.routes.map(({ to, exact, component }, index) => (
      <Route path={to} exact={exact} component={component} key={index} />
    ));
  }

  public logout = () => {
    // do nothing
  };
}
