import { ApiContext } from '@syndesis/api';
import { PfVerticalNavItem } from '@syndesis/ui';
import * as React from 'react';
import {
  Route,
  RouteComponentProps,
  Switch,
  withRouter,
} from 'react-router-dom';
import './App.css';
import { ConnectionsModule } from '../modules/connections';
import { DashboardModule } from '../modules/dashboard';
import { IntegrationsModule } from '../modules/integrations';
import { AppContext, IAppContext, IAppSettings } from './AppContext';
import { AuthContext, IAuthContext } from './AuthContext';
import { AuthenticatedRoute } from './AuthenticatedRoute';
import { Layout } from './Layout';
import { LoginPage } from './LoginPage';
import { Logout } from './LogoutPage';
import { SettingsPage } from './SettingsPage';
import { TokenPage } from './TokenPage';

const PrivateRoutes = () => (
  <Switch>
    <Route path="/" exact={true} component={DashboardModule} />
    <Route path="/integrations" component={IntegrationsModule} />
    <Route path="/connections" component={ConnectionsModule} />
  </Switch>
);

const WiredLogout = () => (
  <AppContext.Consumer>
    {({ logout }) => <Logout logout={logout} />}
  </AppContext.Consumer>
);

interface IAppState extends IAuthContext, IAppContext {
  firstSetup: boolean;
  lastUsedProject?: string;
}

class AppBase extends React.Component<RouteComponentProps, IAppState> {
  private TokenWithState: any;

  public constructor(props: any) {
    super(props);
    const token = this.getPersistedValue('access_token');
    this.state = {
      apiUri: this.getPersistedValue('apiUri') || '',
      authorizationUri: this.getPersistedValue('authorizationUri') || '',
      clientId: 'camel-k-ui',
      firstSetup: this.getPersistedValue('setupDone') === null,
      lastUsedProject: this.getPersistedValue('lastUsedProject') || 'default',
      logged: !!token,
      logout: this.logout,
      redirectUri: document.location!.origin + '/token',
      responseType: 'token',
      saveSettings: this.saveSettings,
      token,
    } as IAppState;

    this.TokenWithState = () => <TokenPage to="/" onToken={this.updateToken} />;

    if (this.state.firstSetup) {
      this.props.history.replace('/settings');
    }
  }

  public render() {
    return (
      <>
        <AppContext.Provider value={this.state}>
          <AuthContext.Provider value={this.state}>
            <ApiContext.Provider
              value={{
                apiUri: this.state.apiUri,
                token: this.state.token,
              }}
            >
              <Layout navbar={this.renderNavbar()}>
                <React.Fragment>
                  <Switch>
                    <Route path="/token" render={this.TokenWithState} />
                    <Route path="/login" component={LoginPage} />
                    <Route path="/logout" component={WiredLogout} />
                    <Route path="/settings" component={SettingsPage} />
                    <AuthenticatedRoute path="/" component={PrivateRoutes} />
                  </Switch>
                </React.Fragment>
              </Layout>
            </ApiContext.Provider>
          </AuthContext.Provider>
        </AppContext.Provider>
      </>
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

  private updateToken = (token: string) => {
    this.storeToken(token);
    this.setState({
      logged: !!token,
      token,
    });
  };

  private storeToken = (token: string): void => {
    return localStorage.setItem('access_token', token);
  };

  private saveSettings = (settings: IAppSettings): void => {
    const state = {
      ...settings,
      firstSetup: false,
      logged: false,
      token: null,
    };
    this.setState(state);
    localStorage.setItem('apiUri', state.apiUri);
    localStorage.setItem('authorizationUri', state.authorizationUri);
    localStorage.setItem('setupDone', `${Date.now()}`);
    this.props.history.replace('/login');
  };

  private getPersistedValue = (property: string): string | null => {
    return localStorage.getItem(property);
  };

  private logout = (): void => {
    this.setState({
      logged: false,
      token: null,
    });
    this.props.history.replace('/');
  };
}

export const App = withRouter(AppBase);
