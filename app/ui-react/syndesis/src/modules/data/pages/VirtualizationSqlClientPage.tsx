import { RestDataService } from '@syndesis/models';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { HeaderView } from '../shared';
import { VirtualizationNavBar } from '../shared';

/**
 * @param virtualizationId - the ID of the virtualization whose details are being shown by this page.
 */
export interface IVirtualizationSqlClientPageRouteParams {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * @param virtualizationId - the virtualization whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationSqlClientPageRouteParams#virtualizationId}.
 */

export interface IVirtualizationSqlClientPageRouteState {
  virtualization: RestDataService;
}

export class VirtualizationSqlClientPage extends React.Component {
  public render() {
    return (
      <WithRouteData<
        IVirtualizationSqlClientPageRouteParams,
        IVirtualizationSqlClientPageRouteState
      >>
        {({ virtualizationId }, { virtualization }, { history }) => {
          return (
            <div>
              <HeaderView virtualizationId={virtualizationId} />
              <Translation ns={['data', 'shared']}>
                {t => <VirtualizationNavBar virtualization={virtualization} />}
              </Translation>
              <h3>SQL Client page goes here.</h3>
            </div>
          );
        }}
      </WithRouteData>
    );
  }
}
