import { RestDataService } from '@syndesis/models';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
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

export class VirtualizationMetricsPage extends React.Component<
  IVirtualizationMetricsPageRouteParams,
  IVirtualizationMetricsPageRouteState
> {
  public render() {
    return (
      <WithRouteData<
        IVirtualizationMetricsPageRouteParams,
        IVirtualizationMetricsPageRouteState
      >>
        {({ virtualizationId }, { virtualization }, { history }) => {
          return (
            <div>
              <HeaderView virtualizationId={virtualizationId} />
              <Translation ns={['data', 'shared']}>
                {t => <VirtualizationNavBar virtualization={virtualization} />}
              </Translation>
              <h3>Metrics page goes here.</h3>
            </div>
          );
        }}
      </WithRouteData>
    );
  }
}
