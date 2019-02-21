import {
  ApiContext,
  ServerEventsContext,
  WithServerEvents,
} from '@syndesis/api';
import { IntegrationMonitoring } from '@syndesis/models';
import {
  AppLayout,
  Loader,
  PfNavLink,
  PfVerticalNavItem,
  UnrecoverableError,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import { TranslationFunction } from 'i18next';
import { Icon, Masthead } from 'patternfly-react';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import { Route, RouteComponentProps, Switch } from 'react-router-dom';
import './App.css';
import { AppContext } from './AppContext';
import { IConfigFile, WithConfig } from './WithConfig';

import pictogram from './glasses_logo_square.png';
import typogram from './syndesis-logo-svg-white.svg';

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

export interface IAppBaseState {
  showNavigation: boolean;
}

export class App extends React.Component<IAppBaseProps, IAppBaseState> {
  public state = {
    showNavigation: true,
  };

  constructor(props: IAppBaseProps) {
    super(props);
    this.logout = this.logout.bind(this);
    this.getPodLogUrl = this.getPodLogUrl.bind(this);
    this.hideNavigation = this.hideNavigation.bind(this);
    this.showNavigation = this.showNavigation.bind(this);
  }

  public logout() {
    // do nothing
  }

  public getPodLogUrl(
    config: IConfigFile,
    monitoring: IntegrationMonitoring | undefined
  ): string | undefined {
    if (
      !config ||
      !monitoring ||
      !monitoring.linkType ||
      !monitoring.namespace ||
      !monitoring.podName
    ) {
      return undefined;
    }
    const baseUrl = `${config.consoleUrl}/project/${
      monitoring.namespace
    }/browse/pods/${monitoring.podName}?tab=`;
    switch (monitoring.linkType) {
      case 'LOGS':
        return baseUrl + 'logs';
      case 'EVENTS':
        return baseUrl + 'events';
      default:
        return undefined;
    }
  }

  public hideNavigation(): void {
    this.setState({
      showNavigation: false,
    });
  }

  public showNavigation(): void {
    this.setState({
      showNavigation: true,
    });
  }

  public renderAppNav(t: TranslationFunction) {
    return (
      <Masthead.Dropdown
        id="app-user-dropdown"
        title={[
          <span className="dropdown-title" key="dropdown-title">
            <Icon type={'fa'} name={'user'} /> developer
          </span>,
        ]}
      >
        <PfNavLink to={'/logout'} label={t('Logout')} />
      </Masthead.Dropdown>
    );
  }

  public renderVerticalNav() {
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

  public render() {
    return (
      <NamespacesConsumer ns={['app']}>
        {t => (
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
                      getPodLogUrl: this.getPodLogUrl,
                      hideNavigation: this.hideNavigation,
                      logout: this.logout,
                      showNavigation: this.showNavigation,
                    }}
                  >
                    <ApiContext.Provider
                      value={{
                        apiUri: `${config!.apiBase}${config!.apiEndpoint}`,
                        dvApiUri: `${config!.apiBase}${config!.datavirt.dvUrl}`,
                        headers: { 'SYNDESIS-XSRF-TOKEN': 'awesome' },
                      }}
                    >
                      <ApiContext.Consumer>
                        {({ apiUri, headers }) => (
                          <WithServerEvents apiUri={apiUri} headers={headers}>
                            {functions => (
                              <ServerEventsContext.Provider value={functions}>
                                <AppLayout
                                  appTitle={'Syndesis'}
                                  appNav={this.renderAppNav(t)}
                                  verticalNav={this.renderVerticalNav()}
                                  pictograph={pictogram}
                                  typogram={typogram}
                                  logoHref={'/'}
                                  showNavigation={this.state.showNavigation}
                                  onNavigationCollapse={this.hideNavigation}
                                  onNavigationExpand={this.showNavigation}
                                >
                                  <React.Fragment>
                                    <Switch>{this.renderRoutes()}</Switch>
                                  </React.Fragment>
                                </AppLayout>
                              </ServerEventsContext.Provider>
                            )}
                          </WithServerEvents>
                        )}
                      </ApiContext.Consumer>
                    </ApiContext.Provider>
                  </AppContext.Provider>
                )}
              </WithLoader>
            )}
          </WithConfig>
        )}
      </NamespacesConsumer>
    );
  }
}
