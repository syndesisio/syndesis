import { WithApiProvider } from '@syndesis/api';
import * as H from '@syndesis/history';
import {
  ApiProviderReviewActions,
  IntegrationEditorLayout,
  PageLoader,
  PageSection,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { ApiError, PageTitle } from '../../../../../shared';
import {
  IBaseApiProviderRouteParams,
  IReviewActionsRouteState,
} from '../interfaces';

export interface IReviewActionsPageProps {
  cancelHref: (
    p: IBaseApiProviderRouteParams,
    s: IReviewActionsRouteState
  ) => H.LocationDescriptor;
}

/**
 * This is the page where a user reviews the actions that have been
 * extracted from the API specification previously created or provided
 * earlier in the API Provider editor.
 */
export class ReviewActionsPage extends React.Component<
  IReviewActionsPageProps
> {
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <WithRouteData<IBaseApiProviderRouteParams, IReviewActionsRouteState>>
            {(params, state) => (
              <>
                <PageTitle
                  title={t('integrations:apiProvider:reviewActions:title')}
                />
                <IntegrationEditorLayout
                  title={t('integrations:apiProvider:reviewActions:title')}
                  description={t(
                    'integrations:apiProvider:reviewActions:description'
                  )}
                  content={
                    <WithApiProvider specification={state.specification}>
                      {({ data, loading, error }) => (
                        <PageSection>
                          <WithLoader
                            loading={loading}
                            loaderChildren={<PageLoader />}
                            error={error}
                            errorChildren={<ApiError />}
                          >
                            {() => <ApiProviderReviewActions />}
                          </WithLoader>
                        </PageSection>
                      )}
                    </WithApiProvider>
                  }
                  cancelHref={this.props.cancelHref(params, state)}
                />
              </>
            )}
          </WithRouteData>
        )}
      </Translation>
    );
  }
}
