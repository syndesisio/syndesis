import { useExtensionHelpers } from '@syndesis/api';
import { Action } from '@syndesis/models';
import {
  Breadcrumb,
  ButtonLink,
  ExtensionImportCard,
  ExtensionImportReview,
  IImportAction,
  Loader,
  PageSection,
  SimplePageHeader,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../app';
import resolvers from '../../resolvers';
import { getExtensionTypeName } from '../utils';

export interface IExtensionImportRouteProps {
  extensionId?: string;
}

export const ExtensionImportPage: React.FunctionComponent = () => {
  const { params, history } = useRouteData<IExtensionImportRouteProps, null>();
  const { pushNotification } = React.useContext(UIContext);
  const { importExtension, uploadExtension } = useExtensionHelpers();
  const { t } = useTranslation(['extensions', 'shared']);
  const [disableDropzone, setDisableDropzone] = React.useState(false);
  const [loading, setLoading] = React.useState(false);
  const [dndUploadFailedMessage, setDndUploadFailedMessage] = React.useState();
  const [
    dndUploadSuccessMessage,
    setDndUploadSuccessMessage,
  ] = React.useState();
  const [extension, setExtension] = React.useState();
  const [i18nAlertMessage, setI18nAlertMessage] = React.useState();

  /**
   * Obtains a localized label for the extension actions.
   */
  const getActionsLabel = (): string => {
    if (extension) {
      switch (extension.extensionType) {
        case 'Steps':
          return t('shared:Steps');
        default:
          break;
      }
    }

    return t('shared:Actions');
  };

  /**
   * Obtains a localized message describing the action.
   * @param name the action name
   * @param description the action description
   */
  const getActionText = (name: string, description: string): string => {
    return t('action', {
      actionDescription: description,
      actionName: name,
    });
  };

  const onDndUploadAcceptedHandler = async (files: File[]) => {
    try {
      // set state before call to backend
      setDisableDropzone(true);
      setDndUploadFailedMessage(undefined);
      setDndUploadSuccessMessage(undefined);
      setI18nAlertMessage(undefined);
      setLoading(true);

      // make server call to update existing or to create a new extension
      const uploaded = params.extensionId
        ? await uploadExtension(files[0], params.extensionId)
        : await uploadExtension(files[0]);

      pushNotification(
        t('extension.extensionUploadSuccessMessage', {
          extensionName: uploaded.name,
        }),
        'success'
      );

      // set state based on successful upload
      setDndUploadSuccessMessage(t('extension.importUploadSuccessMessage'));
      setExtension(uploaded);
      setLoading(false);
    } catch (e) {
      // set state based on failed upload
      setDisableDropzone(false);
      setDndUploadFailedMessage(t('extension.importUploadFailedMessage'));
      setI18nAlertMessage(t('extension.importInvalidFileMessage'));
      setLoading(false);
    }
  };

  const onDndUploadRejectedHandler = (failedFileName: string) => {
    const text = t('extension.importUploadFailedAlertMessage', {
      fileName: failedFileName,
    });
    pushNotification(text, 'error');
    return text;
  };

  const handleImport = async (importExtensionId: string) => {
    try {
      await importExtension(importExtensionId);
      pushNotification(t('extension.extensionImportSuccessMessage'), 'success');
      history.push(resolvers.extensions.list());
    } catch (e) {
      pushNotification(
        t('extension.extensionImportFailedMessage', {
          extensionId: importExtensionId,
        }),
        'error'
      );
    }
  };

  return (
    <>
      <Breadcrumb
        actions={
          <ButtonLink
            data-testid={'extension-import-page-cancel-button'}
            className={'extension-import-page__action'}
            href={resolvers.extensions.list()}
            as={'default'}
          >
            {t('shared:Cancel')}
          </ButtonLink>
        }
      >
        <Link
          data-testid={'extension-import-page-home-link'}
          to={resolvers.dashboard.root()}
        >
          {t('shared:Home')}
        </Link>
        <Link
          data-testid={'extension-import-page-extensions-link'}
          to={resolvers.extensions.list()}
        >
          {t('shared:Extensions')}
        </Link>
        <span>{t('extension.extensionImportPageTitle')}</span>
      </Breadcrumb>
      <SimplePageHeader
        i18nTitle={t('extension.ImportExtension')}
        i18nDescription={t('extension.importUpdateMessage')}
      />
      <PageSection>
        {loading ? <Loader /> : null}
        <ExtensionImportCard
          dndDisabled={disableDropzone}
          i18nAlertMessage={i18nAlertMessage}
          i18nDndHelpMessage={t('extension.importHelpMessage')}
          i18nDndInstructions={t('extension.importDndInstructions')}
          i18nDndNoFileSelectedMessage={t(
            'extension.importNoFileSelectedMessage'
          )}
          i18nDndSelectedFileLabel={t('extension.importSelectedFileLabel')}
          i18nDndUploadFailedMessage={dndUploadFailedMessage}
          i18nDndUploadSuccessMessage={dndUploadSuccessMessage}
          onDndUploadAccepted={onDndUploadAcceptedHandler}
          onDndUploadRejected={onDndUploadRejectedHandler}
        />
        {extension && extension.id && extension.extensionId ? (
          <ExtensionImportReview
            actions={(extension.actions || []).map(
              (action: Action) =>
                ({
                  description: action.description,
                  name: action.name,
                } as IImportAction)
            )}
            cancelLink={resolvers.extensions.list()}
            extensionDescription={extension.description}
            extensionId={extension.extensionId}
            extensionName={extension.name}
            extensionUid={extension.id}
            i18nActionsLabel={getActionsLabel()}
            i18nCancel={t('shared:Cancel')}
            i18nDescriptionLabel={t('shared:Description')}
            i18nExtensionTypeMessage={getExtensionTypeName(extension)}
            i18nIdLabel={t('shared:ID')}
            i18nImport={t('extensions:extension.ImportExtension')}
            i18nNameLabel={t('shared:Name')}
            i18nTitle={t('extensions:extension.ImportReview')}
            i18nTypeLabel={t('shared:Type')}
            i18nActionText={getActionText}
            onImport={handleImport}
          />
        ) : null}
      </PageSection>
    </>
  );
};
