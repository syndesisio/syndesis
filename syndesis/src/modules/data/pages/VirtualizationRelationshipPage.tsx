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
 * Translationeless to ensure that we are
 * Translationwill be used when navigating from the
 * Translationge}.
 */

export interface IVirtualizationDetailRouteState {
  virtualization?: RestDataService;
}

export default class VirtualizationRelationshipPage extends React.Component {
  public render() {
    return (
      <Translation ns={['data', 'shared']}>
        {t => <VirtualizationNavBar />}
      </Translation>
    );
    return 'TODO - Implement Relationship page';
  }
}
