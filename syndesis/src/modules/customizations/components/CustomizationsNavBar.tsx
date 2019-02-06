import { PfNavLink } from '@syndesis/ui';
import { Nav } from 'patternfly-react';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import routes from '../routes';

/**
 * A component that displays a nav bar with 2 items:
 *
 * 1. a link to the page that displays a list of API Connectors, and
 * 2. a link to the page that displays a list of extensions.
 *
 * @see [ApiConnectorsPage]{@link ../pages/ApiConnectorsPage}
 * @see [ExtensionsPage]{@link ../pages/ExtensionsPage}
 */
export default class CustomizationsNavBar extends React.Component {
  public render() {
    return (
      <NamespacesConsumer ns={['customizations', 'shared']}>
        {t => (
          <Nav
            bsClass="nav nav-tabs nav-tabs-pf"
            style={{
              background: '#fff',
            }}
          >
            <PfNavLink
              label={t('apiConnector.apiConnectorsPageTitle')}
              to={routes.apiConnectors.list}
              style={{
                marginLeft: 20,
              }}
            />
            <PfNavLink
              label={t('extension.extensionsPageTitle')}
              to={routes.extensions.list}
            />
          </Nav>
        )}
      </NamespacesConsumer>
    );
  }
}
