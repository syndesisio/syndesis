import { ApicurioAdapter } from '@syndesis/apicurio-adapter';
import * as H from '@syndesis/history';
import { IframeWrapper, IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../../shared';
import {
  IApiProviderReviewActionsRouteState,
  IBaseApiProviderRouteParams,
} from '../interfaces';

export interface IEditSpecificationPageProps {
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
> = ({ cancelHref, saveHref }) => {
  const [specification, setSpecification] = React.useState<string | undefined>(
    undefined
  );
  const onSpecification = (newSpec: any) => {
    setSpecification(JSON.stringify(newSpec.spec));
  };

  return (
    <Translation ns={['integrations', 'shared']}>
      {t => (
        <WithRouteData<
          IBaseApiProviderRouteParams,
          IApiProviderReviewActionsRouteState
        >>
          {(params, state) => (
            <>
              <PageTitle
                title={t('integrations:apiProvider:editSpecification:title')}
              />
              <IntegrationEditorLayout
                title={t('integrations:apiProvider:editSpecification:title')}
                description={t(
                  'integrations:apiProvider:editSpecification:description'
                )}
                content={
                  <IframeWrapper>
                    <ApicurioAdapter
                      specification={specification || state.specification}
                      onSpecification={onSpecification}
                    />
                  </IframeWrapper>
                }
                cancelHref={cancelHref(params, state)}
                saveHref={saveHref(params, {
                  ...state,
                  specification: specification || state.specification,
                })}
              />
            </>
          )}
        </WithRouteData>
      )}
    </Translation>
  );
};
