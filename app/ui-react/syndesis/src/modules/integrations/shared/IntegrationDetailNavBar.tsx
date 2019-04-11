import { Container, TabBar, TabBarItem } from '@syndesis/ui';
import * as H from 'history';
import * as React from 'react';
import { Translation } from 'react-i18next';

export interface IIntegrationDetailNavBarProps {
  detailsTabHref: H.LocationDescriptor;
  activityTabHref: H.LocationDescriptor;
}

/**
 * A component that displays a nav bar with 3 items, only showing 1 for now:
 *
 * 1. a link to the page that displays the integration with the Details tab enabled,
 * 2. a link to the page that displays the integration with the Activity tab enabled.
 *
 *
 * @see [DetailsPage]{@link ../pages/detail/DetailsPage}
 * @see [ActivityPage]{@link ../pages/detail/ActivityPage}
 */
export class IntegrationDetailNavBar extends React.Component<
  IIntegrationDetailNavBarProps
> {
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
              <TabBarItem label={'Details'} to={this.props.detailsTabHref} />
              <TabBarItem label={'Activity'} to={this.props.activityTabHref} />
            </TabBar>
          </Container>
        )}
      </Translation>
    );
  }
}
