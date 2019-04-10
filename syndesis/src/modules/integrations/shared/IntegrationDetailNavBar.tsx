import { Container, TabBar, TabBarItem } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import routes from '../routes';

/**
 * A component that displays a nav bar with 3 items:
 *
 * 1. a link to the page that displays the integration with the Details tab enabled,
 * 2. a link to the page that displays the integration with the Activity tab enabled, and
 * 3. a link to the page that displays the integration with the Metrics tab enabled.
 *
 *
 * @see [DetailsPage]{@link ../pages/DetailsPage}
 */
export default class IntegrationDetailNavBar extends React.Component {
  public render() {
    return (
      <Translation ns={['integration', 'shared']}>
        {t => (
          <Container
            style={{
              background: '#fff',
            }}
          >
            <TabBar>
              <TabBarItem
                label={t('integration.detailsPageTitle')}
                to={routes.detail.details}
              />
            </TabBar>
          </Container>
        )}
      </Translation>
    );
  }
}
