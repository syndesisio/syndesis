import HeaderView from '../components/HeaderView';
import * as React from 'react';
import { RestDataService } from '@syndesis/models';
import { Translation } from 'react-i18next';
import VirtualizationNavBar from '../components/VirtualizationNavBar';
import { WithRouteData } from '@syndesis/utils';

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
  virtualization: RestDataService;
}

export default class VirtualizationMetricsPage extends React.Component<
  IVirtualizationDetailRouteParams,
  IVirtualizationDetailRouteState
> {
  public render() {
    return (
      <WithRouteData<
        IVirtualizationDetailRouteParams,
        IVirtualizationDetailRouteState
      >>
        {({ virtualizationId }, { virtualization }, { history }) => {
          return (
            <div>
              <HeaderView virtualizationId={virtualizationId} />
              <Translation ns={['data', 'shared']}>
                {t => (
                  <VirtualizationNavBar
                    virtualization={virtualization}
                    virtualizationId={virtualizationId}
                  />
                )}
              </Translation>
              <h3>Metrics page goes here.</h3>
            </div>
          );
        }}
      </WithRouteData>
    );
  }
}
