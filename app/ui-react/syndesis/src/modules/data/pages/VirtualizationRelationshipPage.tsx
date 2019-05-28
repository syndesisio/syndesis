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
export interface IVirtualizationRelationshipPageRouteParams {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * @param virtualizationId - the virtualization whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationRelationshipPageRouteState#virtualizationId}.
 */

export interface IVirtualizationRelationshipPageRouteState {
  virtualization: RestDataService;
}

export class VirtualizationRelationshipPage extends React.Component {
  public render() {
    return (
      <WithRouteData<
        IVirtualizationRelationshipPageRouteParams,
        IVirtualizationRelationshipPageRouteState
      >>
        {({ virtualizationId }, { virtualization }, { history }) => {
          return (
            <Translation ns={['data', 'shared']}>
              {t => (
                <>
                  <Breadcrumb>
                    <Link
                      data-testid={'virtualization-relationship-page-home-link'}
                      to={resolvers.dashboard.root()}
                    >
                      {t('shared:Home')}
                    </Link>
                    <Link
                      data-testid={
                        'virtualization-relationship-page-virtualizations-link'
                      }
                      to={resolvers.data.root()}
                    >
                      {t('shared:DataVirtualizations')}
                    </Link>
                    <span>
                      {virtualizationId + ' '}
                      {t('data:virtualization.relationship')}
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
                    <h2>Relationships are not yet implemented</h2>
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
