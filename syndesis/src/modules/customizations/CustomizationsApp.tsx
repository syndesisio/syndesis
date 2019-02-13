import { PfNavLink } from '@syndesis/ui';
import { Nav } from 'patternfly-react';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import { Redirect, Route, Switch } from 'react-router';
import ApiConnectorsPage from './pages/ApiConnectorsPage';
import ExtensionsPage from './pages/ExtensionsPage';

export interface ICustomizationsAppProps {
  baseurl: string;
}

export default class CustomizationApp extends React.Component<
  ICustomizationsAppProps
> {
  public render() {
    return (
      <NamespacesConsumer ns={['customizations', 'shared']}>
        {t => (
          <>
            <Nav
              bsClass="nav nav-tabs nav-tabs-pf"
              style={{
                background: '#fff',
              }}
            >
              <PfNavLink
                label={t('apiConnector.apiConnectorsPageTitle')}
                to={'/customizations/api-connector'}
                style={{
                  marginLeft: 20,
                }}
              />
              <PfNavLink
                label={t('extension.extensionsPageTitle')}
                to={'/customizations/extensions'}
              />
            </Nav>
            <Switch>
              <Redirect
                path={this.props.baseurl}
                exact={true}
                to={`${this.props.baseurl}/api-connector`}
              />
              <Route
                path={`${this.props.baseurl}/api-connector`}
                exact={true}
                component={ApiConnectorsPage}
              />
              <Route
                path={`${this.props.baseurl}/extensions`}
                exact={true}
                component={ExtensionsPage}
              />
            </Switch>
          </>
        )}
      </NamespacesConsumer>
    );
  }
}
