import { RestDataService } from '@syndesis/models';
import { Breadcrumb, PageSection, ViewHeader } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
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

export const VirtualizationRelationshipPage: React.FunctionComponent = () => {

  const { t } = useTranslation(['data', 'shared']);
  const { params, state } = useRouteData<
    IVirtualizationRelationshipPageRouteParams,
    IVirtualizationRelationshipPageRouteState
  >();

  return (
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
          {params.virtualizationId + ' '}
          {t('data:virtualization.relationship')}
        </span>
      </Breadcrumb>
      <ViewHeader
        i18nTitle={state.virtualization.keng__id}
        i18nDescription={state.virtualization.tko__description}
      />
      <PageSection variant={'light'} noPadding={true}>
        <VirtualizationNavBar virtualization={state.virtualization} />
      </PageSection>
      <PageSection>
        <h2>Relationships are not yet implemented</h2>
      </PageSection>
    </>
  );
}
