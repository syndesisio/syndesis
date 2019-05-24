import { ApiProviderSelectMethod, PageSection } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../../shared';

/**
 * The very first page of the API Provider editor, where you decide
 * if you want to provide an OpenAPI Spec file via drag and drop, or
 * if you a URL of an OpenAPI spec
 */
export class SelectMethodPage extends React.Component {
  public render() {
    const handleFiles = (files: File[]) => {
      files.forEach(file => {
        return '<span>Process file ' + file.name + '</span>\n';
      });
    };

    const uploadFailedMessage = (fileName: string) => {
      return (
        '<span>File <strong>' +
        fileName +
        '</strong> could not be uploaded</span>'
      );
    };

    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <PageSection>
            <PageTitle
              title={t('integrations:apiProvider:selectMethod:title')}
            />
            <ApiProviderSelectMethod
              disableDropzone={false}
              fileExtensions={t(
                'integrations:apiProvider:selectedMethod:dndFileExtensions'
              )}
              i18nHelpMessage={t(
                'integrations:apiProvider:selectedMethod:dndHelpMessage'
              )}
              i18nInstructions={t(
                'integrations:apiProvider:selectedMethod:dndInstructions'
              )}
              i18nNoFileSelectedMessage={t(
                'integrations:apiProvider:selectedMethod:dndNoFileSelectedMessage'
              )}
              i18nSelectedFileLabel={t(
                'integrations:apiProvider:selectedMethod:dndSelectedFileLabel'
              )}
              i18nUploadFailedMessage={t(
                'integrations:apiProvider:selectedMethod:'
              )}
              i18nUploadSuccessMessage={t(
                'integrations:apiProvider:selectedMethod:'
              )}
              onUploadAccepted={handleFiles}
              onUploadRejected={uploadFailedMessage}
              i18nDescription={t(
                'integrations:apiProvider:selectMethod:description'
              )}
              i18nMethodFromFile={t(
                'integrations:apiProvider:selectMethod:methodFromFile'
              )}
              i18nMethodFromScratch={t(
                'integrations:apiProvider:selectMethod:methodFromScratch'
              )}
              i18nMethodFromUrl={t(
                'integrations:apiProvider:selectMethod:methodFromUrl'
              )}
              i18nUrlNote={t('integrations:apiProvider:selectMethod:urlNote')}
            />
          </PageSection>
        )}
      </Translation>
    );
  }
}
