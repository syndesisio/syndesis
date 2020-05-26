import * as H from '@syndesis/history';
import {
  ApiProviderMethod,
  ApiProviderSelectMethod,
  IntegrationEditorLayout,
  PageSection,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../../shared';
import {
  IBaseApiProviderRouteParams,
  IBaseApiProviderRouteState,
  IPageWithEditorBreadcrumb,
} from '../interfaces';

const EMPTY_API_20: string = `
{
  "swagger": "2.0",
  "info": {
    "title": "Untitled API",
    "description": "",
    "version": "0.0.0"
  },
  "paths": {
  },
  "consumes": [ "application/json" ],
  "produces": [ "application/json" ]
}`;

const EMPTY_API_30: string = `
{
  "openapi": "3.0.2",
  "info": {
    "title": "Untitled API",
    "description": "",
    "version": "0.0.0"
  },
  "paths": {
  }
}
`;

export interface ISelectMethodPageProps extends IPageWithEditorBreadcrumb {
  cancelHref: (
    p: IBaseApiProviderRouteParams,
    s: IBaseApiProviderRouteState
  ) => H.LocationDescriptor;
  getReviewHref: (
    specification: string,
    p: IBaseApiProviderRouteParams,
    s: IBaseApiProviderRouteState
  ) => H.LocationDescriptorObject;
  getEditorHref: (
    specification: string,
    p: IBaseApiProviderRouteParams,
    s: IBaseApiProviderRouteState
  ) => H.LocationDescriptorObject;
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
          <WithRouteData<
            IBaseApiProviderRouteParams,
            IBaseApiProviderRouteState
          >>
            {(params, state, { history }) => {
              const onNext = (
                method: ApiProviderMethod,
                specification: string
              ) => {
                switch (method) {
                  case 'file':
                  case 'url':
                    history.push(
                      this.props.getReviewHref(specification, params, state)
                    );
                    break;
                  case 'scratch3x':
                    history.push(
                      this.props.getEditorHref(EMPTY_API_30, params, state)
                    );
                    break;
                  case 'scratch2x':
                    history.push(
                      this.props.getEditorHref(EMPTY_API_20, params, state)
                    );
                    break;
                  default:
                    throw new Error(`Unknown method specified: ${method}`);
                }
              };

              return (
                <>
                  <PageTitle
                    title={t('integrations:apiProvider:selectMethod:title')}
                  />
                  <IntegrationEditorLayout
                    title={t('integrations:apiProvider:selectMethod:title')}
                    description={t(
                      'integrations:apiProvider:selectMethod:description'
                    )}
                    toolbar={this.props.getBreadcrumb(
                      t('integrations:apiProvider:selectMethod:title'),
                      params,
                      state
                    )}
                    content={
                      <PageSection>
                        <ApiProviderSelectMethod
                          disableDropzone={false}
                          fileExtensions={t(
                            'integrations:apiProvider:selectMethod:dndFileExtensions'
                          )}
                          i18nBtnNext={t('shared:Next')}
                          i18nHelpMessage={t(
                            'integrations:apiProvider:selectMethod:dndHelpMessage'
                          )}
                          i18nInstructions={t(
                            'integrations:apiProvider:selectMethod:dndInstructions'
                          )}
                          i18nNoFileSelectedMessage={t(
                            'integrations:apiProvider:selectMethod:dndNoFileSelectedLabel'
                          )}
                          i18nSelectedFileLabel={t(
                            'integrations:apiProvider:selectMethod:dndSelectedFileLabel'
                          )}
                          i18nUploadFailedMessage={t(
                            'integrations:apiProvider:selectMethod:dndUploadFailedMessage'
                          )}
                          i18nUploadSuccessMessage={t(
                            'integrations:apiProvider:selectMethod:dndUploadSuccessMessage'
                          )}
                          i18nMethodFromFile={t(
                            'integrations:apiProvider:selectMethod:methodFromFile'
                          )}
                          i18nMethodFromScratch2x={t(
                            'integrations:apiProvider:selectMethod:methodFromScratch2x'
                          )}
                          i18nMethodFromScratch3x={t(
                            'integrations:apiProvider:selectMethod:methodFromScratch3x'
                          )}
                          i18nMethodFromUrl={t(
                            'integrations:apiProvider:selectMethod:methodFromUrl'
                          )}
                          i18nUrlNote={t(
                            'integrations:apiProvider:selectMethod:urlNote'
                          )}
                          onNext={onNext}
                        />
                      </PageSection>
                    }
                    cancelHref={this.props.cancelHref(params, state)}
                  />
                </>
              );
            }}
          </WithRouteData>
        )}
      </Translation>
    );
  }
}
