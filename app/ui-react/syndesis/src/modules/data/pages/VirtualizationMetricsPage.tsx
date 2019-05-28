import { RestDataService } from '@syndesis/models';
import { Breadcrumb, PageSection, ViewHeader } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import resolvers from '../../resolvers';
import { VirtualizationNavBar } from '../shared';

/**
 * @param virtualizationId - the ID of the virtualization whose details are being shown by this page.
 */
export interface IVirtualizationMetricsPageRouteParams {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * @param virtualizationId - the virtualization whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationMetricsPageRouteParams#virtualizationId}.
 */

export interface IVirtualizationMetricsPageRouteState {
  virtualization: RestDataService;
}

export class VirtualizationMetricsPage extends React.Component {
  public render() {
    return (
      <WithRouteData<
        IVirtualizationMetricsPageRouteParams,
        IVirtualizationMetricsPageRouteState
      >>
        {({ virtualizationId }, { virtualization }, { history }) => {
          return (
            <Translation ns={['data', 'shared']}>
              {t => (
                <>
                  <Breadcrumb>
                    <Link
                      data-testid={'virtualization-metrics-page-home-link'}
                      to={resolvers.dashboard.root()}
                    >
                      {t('shared:Home')}
                    </Link>
                    <Link
                      data-testid={
                        'virtualization-metrics-page-virtualizations-link'
                      }
                      to={resolvers.data.root()}
                    >
                      {t('shared:DataVirtualizations')}
                    </Link>
                    <span>
                      {virtualizationId + ' '}
                      {t('data:virtualization.metrics')}
                    </span>
                  </Breadcrumb>
                  <ViewHeader
                    i18nTitle={virtualization.keng__id}
                    i18nDescription={virtualization.tko__description}
                  />
                  <PageSection variant={'light'} noPadding={true}>
                    <VirtualizationNavBar virtualization={virtualization} />
                  </PageSection>
                  <PageSection>
                    <h2>Metrics are not yet implemented</h2>
                  </PageSection>
                </>
              )}
            </Translation>
          );
        }}
      </WithRouteData>
    );
  }
}
