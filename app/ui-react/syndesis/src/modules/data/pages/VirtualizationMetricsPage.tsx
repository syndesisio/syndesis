import { RestDataService } from '@syndesis/models';
import { Breadcrumb, Container } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import resolvers from '../../resolvers';
import { HeaderView } from '../shared';
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
                <div>
                  <Breadcrumb>
                    <Link to={resolvers.dashboard.root()}>
                      {t('shared:Home')}
                    </Link>
                    <Link to={resolvers.data.root()}>
                      {t('shared:DataVirtualizations')}
                    </Link>
                    <span>
                      {virtualizationId + ' '}
                      {t('data:virtualization.metrics')}
                    </span>
                  </Breadcrumb>
                  <HeaderView virtualizationId={virtualizationId} />
                  <VirtualizationNavBar virtualization={virtualization} />
                  <Container>
                    <h3>Metrics are not yet implemented</h3>
                  </Container>
                </div>
              )}
            </Translation>
          );
        }}
      </WithRouteData>
    );
  }
}
