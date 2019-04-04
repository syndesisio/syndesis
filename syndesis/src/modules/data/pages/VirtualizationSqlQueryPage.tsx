import { RestDataService } from '@syndesis/models';
import * as React from 'react';
import { Translation } from 'react-i18next';
import VirtualizationNavBar from '../components/VirtualizationNavBar';

/**
 * @param virtualizationId - the ID of the virtualization whose details are being shown by this page.
 */
export interface IVirtualizationDetailRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualizationId - the extension whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationDetailRouteParams#virtualizationId}.
 * This is used to immediately show the details to the user, without
 * any loader; the backend will be called nonetheless to ensure that we are
 * working with the latest data available. This will be used when navigating from the
 * [VirtualizationsPage]{@linVirtualizationViewsPage.1k VirtualizationsPage}.
 */
export interface IVirtualizationDetailRouteState {
  virtualization?: RestDataService;
}

export default class VirtualizationSqlQueryPage extends React.Component {
  public render() {
    return (
      <Translation ns={['data', 'shared']}>
        {t => <VirtualizationNavBar />}
      </Translation>
    );
    return 'TODO - Implement SQL Query page';
  }
}
