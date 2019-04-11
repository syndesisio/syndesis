import { Integration } from '@syndesis/models';
import { Container, TabBar, TabBarItem } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import resolvers from '../resolvers';

/**
 * @param integration - the integration being displayed. If it
 * exists, it must equal to the [integrationId]{@link IIntegrationDetailNavBarProps#integration}.
 */
export interface IIntegrationDetailNavBarProps {
  integration: Integration;
}

/**
 * A component that displays a nav bar with 3 items, only showing 1 for now:
 *
 * 1. a link to the page that displays the integration with the Details tab enabled,
 * 2. a link to the page that displays the integration with the Activity tab enabled, and
 * 3. a link to the page that displays the integration with the Metrics tab enabled.
 *
 *
 * @see [DetailsPage]{@link ../pages/detail/DetailsPage}
 * @see [ActivityPage]{@link ../pages/detail/ActivityPage}
 * @see [MetricsPage]{@link ../pages/detail/MetricsPage}
 */
export class IntegrationDetailNavBar extends React.Component<
  IIntegrationDetailNavBarProps
> {
  public render() {
    const integration = this.props.integration;
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
                label={'Details'}
                to={resolvers.integration.details({
                  integration,
                })}
              />
              <TabBarItem
                label={'Activity'}
                to={resolvers.integration.activity({
                  integration,
                })}
              />
              <TabBarItem
                label={'Metrics'}
                to={resolvers.integration.metrics({
                  integration,
                })}
              />
            </TabBar>
          </Container>
        )}
      </Translation>
    );
  }
}
