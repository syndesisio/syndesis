import { Container, TabBar, TabBarItem } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
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
      <Translation ns={['customizations', 'shared']}>
        {t => (
          <Container
            style={{
              background: '#fff',
            }}
          >
            <TabBar>
              <TabBarItem
                label={t('apiConnector.apiConnectorsPageTitle')}
                to={routes.apiConnectors.list}
              />
              <TabBarItem
                label={t('extension.extensionsPageTitle')}
                to={routes.extensions.list}
              />
            </TabBar>
          </Container>
        )}
      </Translation>
    );
  }
}
