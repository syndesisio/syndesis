import { useApiProviderSpecification } from '@syndesis/api';
import { ApicurioAdapter } from '@syndesis/apicurio-adapter';
import * as H from '@syndesis/history';
import {
  IframeWrapper,
  IntegrationEditorLayout,
  PageLoader,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { ApiError, PageTitle } from '../../../../../shared';
import {
  IApiProviderReviewActionsRouteState,
  IBaseApiProviderRouteParams,
  IPageWithEditorBreadcrumb,
} from '../interfaces';

export interface IEditSpecificationPageProps extends IPageWithEditorBreadcrumb {
  cancelHref: (
    p: IBaseApiProviderRouteParams,
    s: IApiProviderReviewActionsRouteState
  ) => H.LocationDescriptor;
  saveHref: (
    p: IBaseApiProviderRouteParams,
    s: IApiProviderReviewActionsRouteState
  ) => H.LocationDescriptor;
}

/**
 * This is the page where you define or edit your API specification.
 * At the moment, we are using Apicurio as the API specification editor.
 */
export const EditSpecificationPage: React.FunctionComponent<
  IEditSpecificationPageProps
> = ({ cancelHref, saveHref, getBreadcrumb }) => {
  const { params, state } = useRouteData<
    IBaseApiProviderRouteParams,
    IApiProviderReviewActionsRouteState
  >();
  const { specification, loading, error } = useApiProviderSpecification(
    state.specification
  );

  const [updatedSpecification, setUpdatedSpecification] = React.useState();

  const onSpecification = (newSpec: any) => {
    setUpdatedSpecification(JSON.stringify(newSpec.spec));
  };

  return (
    <Translation ns={['integrations', 'shared']}>
      {t => (
        <>
          <PageTitle
            title={t('integrations:apiProvider:editSpecification:title')}
          />
          <IntegrationEditorLayout
            title={t('integrations:apiProvider:editSpecification:title')}
            description={t(
              'integrations:apiProvider:editSpecification:description'
            )}
            toolbar={getBreadcrumb(
              t('integrations:apiProvider:editSpecification:title'),
              params,
              state
            )}
            content={
              <IframeWrapper>
                <WithLoader
                  loading={loading}
                  loaderChildren={<PageLoader />}
                  error={error !== false}
                  errorChildren={<ApiError error={error as Error} />}
                >
                  {() => (
                    <ApicurioAdapter
                      specification={updatedSpecification || specification!}
                      onSpecification={onSpecification}
                    />
                  )}
                </WithLoader>
              </IframeWrapper>
            }
            cancelHref={cancelHref(params, state)}
            saveHref={saveHref(params, {
              ...state,
              specification: updatedSpecification || specification,
            })}
          />
        </>
      )}
    </Translation>
  );
};
