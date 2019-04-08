import { Container, TabBar, TabBarItem } from '@syndesis/ui';
import * as React from 'react';
import { RestDataService } from '@syndesis/models';
import { Translation } from 'react-i18next';
import { WithRouteData } from '@syndesis/utils';
import resolvers from '../resolvers';

/**
 * @param virtualizationId - the ID of the virtualization whose details are being shown by this page.
 */
export interface IVirtualizationDetailRouteParams {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * @param virtualizationId - the virtualization whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationDetailRouteParams#virtualizationId}.
 */

export interface IVirtualizationDetailRouteState {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * A component that displays a nav bar with 4 items:
 *
 * 1. a link to the page that displays a list of Views
 * 2. a link to the page that displays relationships
 * 3. a link to the page that displays the SQL Query editor
 * 4. a link to the page that dispays metrics
 *
 */
export default class VirtualizationNavBar extends React.Component<
  IVirtualizationDetailRouteState,
  IVirtualizationDetailRouteParams
> {
  public render() {
    return (
      <WithRouteData<
        IVirtualizationDetailRouteParams,
        IVirtualizationDetailRouteState
      >>
        {({ virtualizationId }, { virtualization }, { history }) => {
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
                      to={resolvers.virtualizations.views({ virtualization })}
                    />
                    <TabBarItem
                      label={t('data:virtualization.relationship')}
                      to={resolvers.virtualizations.relationship({
                        virtualization,
                      })}
                    />
                    <TabBarItem
                      label={t('data:virtualization.sqlClient')}
                      to={resolvers.virtualizations.sqlClient({
                        virtualization,
                      })}
                    />
                    <TabBarItem
                      label={t('data:virtualization.metrics')}
                      to={resolvers.virtualizations.metrics({ virtualization })}
                    />
                  </TabBar>
                </Container>
              )}
            </Translation>
          );
        }}
      </WithRouteData>
    );
  }
}
