import * as H from '@syndesis/history';
import {
  ApiProviderSelectMethod,
  IntegrationEditorLayout,
  PageSection,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../../shared';
import {
  IConfigureStepRouteParams,
  IConfigureStepRouteState,
} from '../interfaces';

export interface ISelectMethodPageProps {
  cancelHref: (
    p: IConfigureStepRouteParams,
    s: IConfigureStepRouteState
  ) => H.LocationDescriptor;
}

/**
 * The very first page of the API Provider editor, where you decide
 * if you want to provide an OpenAPI Spec file via drag and drop, or
 * if you a URL of an OpenAPI spec
 */
export class SelectMethodPage extends React.Component<ISelectMethodPageProps> {
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <WithRouteData<IConfigureStepRouteParams, IConfigureStepRouteState>>
            {(params, state) => (
              <>
                <PageTitle title={t('integrations:apiProvider:title')} />
                <IntegrationEditorLayout
                  title={t('integrations:apiProvider:title')}
                  description={t('integrations:apiProvider:description')}
                  content={
                    <PageSection>
                      <ApiProviderSelectMethod
                        i18nMethodFromFile={t(
                          'integrations:apiProvider:methodFromFile'
                        )}
                        i18nMethodFromScratch={t(
                          'integrations:apiProvider:methodFromScratch'
                        )}
                        i18nMethodFromUrl={t(
                          'integrations:apiProvider:methodFromUrl'
                        )}
                        i18nUrlNote={t('integrations:apiProvider:urlNote')}
                      />
                    </PageSection>
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
