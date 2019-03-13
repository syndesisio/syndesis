import { Container, TabBar, TabBarItem } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import routes from '../routes';

/**
 * A component that displays a nav bar with 2 items:
 *
 * 1. a link to the page that displays a list of API Connectors, and
 * 1. a link to the page that displays a list of virtualizations.
 *
 * @see [VirtualizationsPage]{@link ../pages/VirtualizationsPage}
 */
export default class VirtualizationNavBar extends React.Component {
  public render() {
    return (
      <Translation ns={['virtualizations', 'shared']}>
        {t => (
          <Container
            style={{
              background: '#fff',
            }}
          >
            <TabBar>
              <TabBarItem
                label={t('virtualization.views')}
                to={routes.virtualizations.virtualization.views}
              />
              <TabBarItem
                label={t('virtualization.sqlClient')}
                to={routes.virtualizations.virtualization.sqlQuery}
              />
              <TabBarItem
                label={t('virtualization.metrics')}
                to={routes.virtualizations.virtualization.metrics}
              />
            </TabBar>
          </Container>
        )}
      </Translation>
    );
  }
}
