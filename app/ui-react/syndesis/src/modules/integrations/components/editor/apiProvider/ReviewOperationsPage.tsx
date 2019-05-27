import * as H from '@syndesis/history';
import { IntegrationEditorLayout, PageSection } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../../shared';
import {
  IBaseApiProviderRouteParams,
  IBaseApiProviderRouteState,
} from '../interfaces';

export interface IReviewOperationsPageProps {
  cancelHref: (
    p: IBaseApiProviderRouteParams,
    s: IBaseApiProviderRouteState
  ) => H.LocationDescriptor;
  getFlowHref: (
    p: IBaseApiProviderRouteParams,
    s: IBaseApiProviderRouteState
  ) => H.LocationDescriptor;
}

/**
 * This is usually the final step of the API Provider user flow.
 * This page shows the operations that have been previously defined
 * earlier in the user flow.
 */
export const ReviewOperationsPage: React.FunctionComponent<
  IReviewOperationsPageProps
> = ({ cancelHref, getFlowHref }) => {
  const { params, state } = useRouteData();

  return (
    <Translation ns={['integrations', 'shared']}>
      {t => (
        <>
          <PageTitle
            title={t('integrations:apiProvider:reviewOperations:title')}
          />
          <IntegrationEditorLayout
            title={t('integrations:apiProvider:reviewOperations:title')}
            description={t(
              'integrations:apiProvider:reviewOperations:description'
            )}
            content={<PageSection />}
            cancelHref={cancelHref(params, state)}
          />
        </>
      )}
    </Translation>
  );
};
