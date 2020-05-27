import { useVirtualizationHelpers } from '@syndesis/api';
import { Breadcrumb, VirtualizationImporter } from '@syndesis/ui';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { PageTitle } from '../../../shared';
import resolvers from '../../resolvers';

export const VirtualizationImportPage: React.FunctionComponent = () => {
  const { importVirtualization } = useVirtualizationHelpers();
  const { t } = useTranslation(['data', 'shared']);

  const [disableDropzone, setDisableDropzone] = React.useState(false);
  const [failedMessages, setFailedMessages] = React.useState([]);
  const [successMessages, setSuccessMessages] = React.useState([]);

  const handleUploadAccepted = async (files: File[]) => {
    setDisableDropzone(true);
    setSuccessMessages([]);
    setFailedMessages([]);

    try {
      await importVirtualization(files[0]);
      setSuccessMessages([
        t('importVirtualizationSuccess', { fileName: files[0].name }),
      ]);
    } catch (error) {
      let details = error;

      if (error.name) {
        const statusCode = parseInt(error.name, 10);

        if (statusCode === 400) {
          // zip does not contain dv.json
          details = t('importVirtualizationZipInvalid');
        } else if (statusCode === 403) {
          // virtualization name is a reserved name or is invalid
          details = t('importVirtualizationNameInvalid');
        } else if (statusCode === 409) {
          // virtualization with that name already exists
          details = t('importVirtualizationExists');
        }
      }

      setFailedMessages([
        t('importVirtualizationFailed', {
          details,
          fileName: files[0].name,
        }),
      ]);
    } finally {
      setDisableDropzone(false);
    }
  };

  const handleUploadRejected = (failed: string) => {
    return t('importUploadFailed', {
      fileName: failed,
    });
  };

  return (
    <>
      <PageTitle title={t('ImportVirtualization')} />
      <Breadcrumb>
        <Link
          data-testid={'virtualization-import-page-home-link'}
          to={resolvers.dashboard.root()}
        >
          {t('shared:Home')}
        </Link>
        <Link
          data-testid={'virtualization-import-page-virtualizations-link'}
          to={resolvers.data.root()}
        >
          {t('shared:Data')}
        </Link>
        <span>{t('ImportVirtualization')}</span>
      </Breadcrumb>
      <VirtualizationImporter
        disableDnd={disableDropzone}
        i18nHelpMessage={t('importVirtualizationHelpMessage')}
        i18nInstructions={t('importVirtualizationInstructions')}
        i18nNoFileSelectedMessage={t('importVirtualizationNoFileSelected')}
        i18nPageDescription={t('importVirtualizationPageDescription')}
        i18nPageTitle={t('ImportVirtualization')}
        i18nSelectedFileLabel={t('importVirtualizationSelectedFileLabel')}
        i18nUploadFailedMessages={failedMessages}
        i18nUploadSuccessMessages={successMessages}
        onUploadAccepted={handleUploadAccepted}
        onUploadRejected={handleUploadRejected}
      />
    </>
  );
};
