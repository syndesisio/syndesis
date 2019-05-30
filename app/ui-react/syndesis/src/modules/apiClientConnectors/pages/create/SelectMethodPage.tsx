import {
  ApiConnectorCreatorLayout,
  Method,
  OpenApiSelectMethod,
  PageSection,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorWizardSteps,
} from '../../components';
import resolvers from '../../resolvers';

export const SelectMethodPage: React.FunctionComponent = () => {
  const { history } = useRouteData();
  const onNext = (method: Method, specification: string) => {
    history.push(resolvers.create.review({ specification }));
  };
  return (
    <Translation ns={['apiClientConnectors', 'shared']}>
      {t => (
        <>
          <PageTitle
            title={t('apiClientConnectors:create:selectMethod:title')}
          />
          <ApiConnectorCreatorBreadcrumb cancelHref={resolvers.list()} />
          <ApiConnectorCreatorLayout
            header={<ApiConnectorCreatorWizardSteps step={1} />}
            content={
              <PageSection>
                <OpenApiSelectMethod
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
                  i18nMethodFromScratch={t(
                    'apiClientConnectors:create:selectMethod:methodFromScratch'
                  )}
                  i18nMethodFromUrl={t(
                    'apiClientConnectors:create:selectMethod:methodFromUrl'
                  )}
                  i18nUrlNote={t(
                    'apiClientConnectors:create:selectMethod:urlNote'
                  )}
                  onNext={onNext}
                  allowFromScratch={false}
                />
              </PageSection>
            }
          />
        </>
      )}
    </Translation>
  );
};
