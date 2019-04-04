import { Container, TabBar, TabBarItem } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import routes from '../routes';

/**
 * A component that displays a nav bar with 4 items:
 *
 * 1. a link to the page that displays a list of Views
 * 2. a link to the page that displays relationships
 * 3. a link to the page that displays the SQL Query editor
 * 4. a link to the page that dispays metrics
 *
 */
export default class VirtualizationNavBar extends React.Component {
  public render() {
    return (
      <Translation ns={['data', 'shared']}>
        {t => (
          <Container
            style={{
              background: '#fff',
            }}
          >
            <TabBar>
              <TabBarItem
                label={t('data:virtualization.views')}
                to={routes.virtualizations.virtualization.views}
              />
              <TabBarItem
                label={t('data:virtualization.relationship')}
                to={routes.virtualizations.virtualization.relationship}
              />
              <TabBarItem
                label={t('data:virtualization.sqlClient')}
                to={routes.virtualizations.virtualization.sqlQuery}
              />
              <TabBarItem
                label={t('data:virtualization.metrics')}
                to={routes.virtualizations.virtualization.metrics}
              />
            </TabBar>
          </Container>
        )}
      </Translation>
    );
  }
}
