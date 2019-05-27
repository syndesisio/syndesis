import * as H from '@syndesis/history';
import {
  ApiProviderReviewOperations,
  ApiProviderReviewOperationsItem,
  IntegrationEditorLayout,
  PageSection,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../../shared';
import {
  IBaseFlowRouteParams,
  IBaseRouteParams,
  IBaseRouteState,
} from '../interfaces';

export interface IReviewOperationsPageProps {
  cancelHref: (p: IBaseRouteParams, s: IBaseRouteState) => H.LocationDescriptor;
  getFlowHref: (
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
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
  const { params, state } = useRouteData<IBaseRouteParams, IBaseRouteState>();
  const flows = state.integration!.flows!.map(f => {
    const [method, description] = (f.description || '').split(' ');
    return {
      ...f,
      description,
      implemented: (f.metadata || {}).excerpt.startsWith('501') ? 0 : 1,
      method,
    };
  });

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
            content={
              <PageSection>
                <ApiProviderReviewOperations>
                  {flows.map((f, idx) => (
                    <ApiProviderReviewOperationsItem
                      key={idx}
                      createFlowHref={getFlowHref(
                        {
                          ...params,
                          flowId: f.id!,
                        },
                        state
                      )}
                      i18nCreateFlow={
                        f.implemented ? 'Edit flow' : 'Create flow'
                      }
                      operationDescription={f.name}
                      operationHttpMethod={f.method}
                      operationPath={f.description}
                    />
                  ))}
                </ApiProviderReviewOperations>
              </PageSection>
            }
            cancelHref={cancelHref(params, state)}
          />
        </>
      )}
    </Translation>
  );
};
