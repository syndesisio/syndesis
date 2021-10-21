import * as React from 'react';

import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorLayout,
  ApiConnectorCreatorSelectMethod,
  ApiConnectorCreatorToggleList,
  PageLoader,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import { ApiError, PageTitle } from '../../../../shared';

import { useApiConnectorSummary } from '@syndesis/api';
import { Translation } from 'react-i18next';
import { UIContext } from '../../../../app';
import resolvers from '../../resolvers';

export const SelectMethodPage: React.FunctionComponent = () => {
  const { history } = useRouteData();
  const [connectorTemplateId, setConnectorTemplateId] = React.useState('');
  const [specification, setSpecification] = React.useState<string | undefined>(
    undefined
  );
  const [url, setUrl] = React.useState<string | undefined>(undefined);
  const [isLoading, setIsLoading] = React.useState(false);
  const { error, loading, setError } = useApiConnectorSummary(
    specification,
    url,
    connectorTemplateId
  );
  const uiContext = React.useContext(UIContext);

  React.useEffect(() => {
    if (error) {
      uiContext.pushNotification((error as Error).message, 'error');
      setError(false);
      setIsLoading(false);
      setSpecification(undefined);
      setUrl(undefined);
      setConnectorTemplateId('');
    }
  }, [error, uiContext, history, setError]);

  const onNext = (
    givenSpecification?: string,
    givenUrl?: string,
    connectorTemplate?: string
  ) => {
    setIsLoading(loading);
    if (connectorTemplate) {
      setConnectorTemplateId(connectorTemplate);
    }
    if (givenSpecification) {
      setSpecification(givenSpecification);
    }
    if (givenUrl) {
      setUrl(givenUrl);
    }
    history.push(
      resolvers.create.review({
        connectorTemplateId: connectorTemplate,
        specification: givenSpecification,
        url: givenUrl,
      })
    );
  };

  return (
    <Translation ns={['apiClientConnectors', 'shared']}>
      {(t) => (
        <>
          <PageTitle
            title={t('apiClientConnectors:create:selectMethod:title')}
          />
          <ApiConnectorCreatorBreadcrumb
            i18nCancel={t('shared:Cancel')}
            i18nConnectors={t('apiClientConnectors:apiConnectorsPageTitle')}
            i18nCreateConnection={t('apiClientConnectors:CreateApiConnector')}
            cancelHref={resolvers.list()}
            connectorsHref={resolvers.list()}
          />
          <WithLoader
            loading={isLoading}
            loaderChildren={<PageLoader />}
            error={error !== false}
            errorChildren={<ApiError error={error as Error} />}
          >
            {() => (
              <ApiConnectorCreatorLayout
                content={
                  <ApiConnectorCreatorSelectMethod
                    disableDropzone={false}
                    fileExtensions={t(
                      'apiClientConnectors:create:selectMethod:dndFileExtensions'
                    )}
                    i18nBtnNext={t('shared:Next')}
                    i18nHelpMessage={t(
                      'apiClientConnectors:create:selectMethod:dndHelpMessage'
                    )}
                    i18nInstructions={t(
                      'apiClientConnectors:create:selectMethod:dndInstructions'
                    )}
                    i18nNoFileSelectedMessage={t(
                      'apiClientConnectors:create:selectMethod:dndNoFileSelectedLabel'
                    )}
                    i18nSelectedFileLabel={t(
                      'apiClientConnectors:create:selectMethod:dndSelectedFileLabel'
                    )}
                    i18nUploadFailedMessage={t(
                      'apiClientConnectors:create:selectMethod:dndUploadFailedMessage'
                    )}
                    i18nUploadSuccessMessage={t(
                      'apiClientConnectors:create:selectMethod:dndUploadSuccessMessage'
                    )}
                    i18nMethodFromFile={t(
                      'apiClientConnectors:create:selectMethod:methodFromFile'
                    )}
                    i18nMethodFromUrl={t(
                      'apiClientConnectors:create:selectMethod:methodFromUrl'
                    )}
                    i18nUrlNote={t(
                      'apiClientConnectors:create:selectMethod:urlNote'
                    )}
                    onNext={onNext}
                  />
                }
                navigation={
                  <ApiConnectorCreatorBreadSteps
                    step={1}
                    i18nConfiguration={t(
                      'apiClientConnectors:create:configuration:title'
                    )}
                    i18nDetails={t('apiClientConnectors:create:details:title')}
                    i18nReview={t('apiClientConnectors:create:review:title')}
                    i18nSecurity={t(
                      'apiClientConnectors:create:security:title'
                    )}
                    i18nSelectMethod={t(
                      'apiClientConnectors:create:selectMethod:title'
                    )}
                  />
                }
                toggle={
                  <ApiConnectorCreatorToggleList
                    step={1}
                    i18nDetails={t('apiClientConnectors:create:details:title')}
                    i18nReview={t('apiClientConnectors:create:review:title')}
                    i18nSecurity={t(
                      'apiClientConnectors:create:security:title'
                    )}
                    i18nSelectMethod={t(
                      'apiClientConnectors:create:selectMethod:title'
                    )}
                  />
                }
              />
            )}
          </WithLoader>
        </>
      )}
    </Translation>
  );
};
