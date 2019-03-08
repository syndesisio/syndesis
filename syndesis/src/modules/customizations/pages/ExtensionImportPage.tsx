import { WithExtensionHelpers } from '@syndesis/api';
import { Action, Extension } from '@syndesis/models';
import {
  Breadcrumb,
  ButtonLink,
  Container,
  ExtensionImportCard,
  ExtensionImportReview,
  IImportAction,
  Loader,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import i18n from '../../../i18n';
import resolvers from '../../resolvers';
import { getExtensionTypeName } from '../customizationsUtils';
import './ExtensionImportPage.css';

export interface IExtensionImportPageState {
  /**
   * `true` if the dropzone should be disabled. Defaults to `false`. When the review component is being
   * shown, the dropzone is disabled.
   */
  disableDropzone: boolean;

  /**
   * A general, localized message for when a file upload is rejected. This is shown in the DnD dropzone.
   */
  dndUploadFailedMessage?: string;

  /**
   * A general, localized message for when a file upload is accepted. This is shown in the DnD dropzone.
   */
  dndUploadSuccessMessage?: string;

  /**
   * The extension created by processing the uploaded file.
   */
  extension?: Extension;

  /**
   * A localized error message indicating that the jar file uploading could not be processed as an extension.
   */
  i18nAlertMessage?: string;

  /**
   * `true` if the extension *.jar file is being processed by the server. Defaults to `false`.
   */
  loading: boolean;
}

export default class ExtensionImportPage extends React.Component<
  {},
  IExtensionImportPageState
> {
  public constructor(props: {}) {
    super(props);
    this.state = { disableDropzone: false, loading: false };
  }

  /**
   * Obtains a localized label for the extension actions.
   */
  public getActionsLabel(): string {
    if (this.state.extension) {
      switch (this.state.extension.extensionType) {
        case 'Steps':
          return i18n.t('shared:Steps');
        default:
          break;
      }
    }

    return i18n.t('shared:Actions');
  }

  /**
   * Obtains a localized message describing the action.
   * @param name the action name
   * @param description the action description
   */
  public getActionText(name: string, description: string): string {
    return i18n.t('customizations:action', {
      actionDescription: description,
      actionName: name,
    });
  }

  public render() {
    const uploadFailedMessage = (failedFileName: string) => {
      this.setState({
        ...this.state,
        dndUploadFailedMessage: i18n.t(
          'customizations:extension.importUploadFailedMessage'
        ),
        dndUploadSuccessMessage: undefined,
      });

      return i18n.t('customizations:extension.importUploadFailedAlertMessage', {
        fileName: failedFileName,
      });
    };
    return (
      <WithRouteData<null, null>>
        {(p, s, { history }) => (
          <WithExtensionHelpers>
            {({ importExtension, uploadExtension }) => {
              const handleUpload = async (files: File[]) => {
                try {
                  // set state before call to backend
                  this.setState({
                    ...this.state,
                    disableDropzone: true,
                    dndUploadFailedMessage: undefined,
                    dndUploadSuccessMessage: undefined,
                    i18nAlertMessage: undefined,
                    loading: true,
                  });

                  // make server call
                  const uploaded = await uploadExtension(files[0]);

                  // set state based on successful upload
                  this.setState({
                    ...this.state,
                    dndUploadSuccessMessage: i18n.t(
                      'customizations:extension.importUploadSuccessMessage'
                    ),
                    extension: uploaded,
                    loading: false,
                  });
                } catch (e) {
                  // set state based on failed upload
                  this.setState({
                    ...this.state,
                    disableDropzone: false,
                    dndUploadFailedMessage: i18n.t(
                      'customizations:extension.importUploadFailedMessage'
                    ),
                    i18nAlertMessage: i18n.t(
                      'customizations:extension.importInvalidFileMessage'
                    ),
                    loading: false,
                  });
                }
              };
              const handleImport = async (extensionId: string) => {
                try {
                  await importExtension(extensionId);
                  history.push(resolvers.customizations.extensions.list());
                } catch (e) {
                  // TODO: post notification
                }
              };
              return (
                <Translation ns={['customizations', 'shared']}>
                  {t => (
                    <>
                      <Container className="col-sm-11">
                        <Breadcrumb>
                          <Link to={resolvers.dashboard.root()}>
                            {t('shared:Home')}
                          </Link>
                          <Link to={resolvers.customizations.root()}>
                            {t('shared:Customizations')}
                          </Link>
                          <Link to={resolvers.customizations.extensions.list()}>
                            {t('shared:Extensions')}
                          </Link>
                          <span>{t('extension.extensionImportPageTitle')}</span>
                        </Breadcrumb>
                      </Container>
                      <Container
                        className={
                          'extension-import-page__actionContainer col-sm-1'
                        }
                      >
                        <ButtonLink
                          className={'extension-import-page__action'}
                          href={resolvers.customizations.extensions.list()}
                          as={'default'}
                        >
                          {t('shared:Cancel')}
                        </ButtonLink>
                      </Container>
                      {this.state.loading ? <Loader /> : null}
                      <ExtensionImportCard
                        dndDisabled={this.state.disableDropzone}
                        i18nAlertMessage={this.state.i18nAlertMessage}
                        i18nDndHelpMessage={t('extension.importHelpMessage')}
                        i18nDndInstructions={t(
                          'extension.importDndInstructions'
                        )}
                        i18nDndNoFileSelectedMessage={t(
                          'extension.importNoFileSelectedMessage'
                        )}
                        i18nDndSelectedFileLabel={t(
                          'extension.importSelectedFileLabel'
                        )}
                        i18nDndUploadFailedMessage={
                          this.state.dndUploadFailedMessage
                        }
                        i18nDndUploadSuccessMessage={
                          this.state.dndUploadSuccessMessage
                        }
                        i18nImportInstructions={t(
                          'extension.importUpdateMessage'
                        )}
                        i18nTitle={t('extension.ImportExtension')}
                        onDndUploadAccepted={handleUpload}
                        onDndUploadRejected={uploadFailedMessage}
                      />
                      {this.state.extension && this.state.extension.id ? (
                        <ExtensionImportReview
                          actions={this.state.extension.actions.map(
                            (action: Action) =>
                              ({
                                description: action.description,
                                name: action.name,
                              } as IImportAction)
                          )}
                          cancelLink={resolvers.customizations.extensions.list()}
                          extensionDescription={
                            this.state.extension.description
                          }
                          extensionId={this.state.extension.id}
                          extensionName={this.state.extension.name}
                          i18nActionsLabel={this.getActionsLabel()}
                          i18nCancel={i18n.t('shared:Cancel')}
                          i18nDescriptionLabel={i18n.t('shared:Description')}
                          i18nExtensionTypeMessage={getExtensionTypeName(
                            this.state.extension
                          )}
                          i18nIdLabel={i18n.t('shared:ID')}
                          i18nImport={i18n.t(
                            'customizations:extension.ImportExtension'
                          )}
                          i18nNameLabel={i18n.t('shared:Name')}
                          i18nTitle={i18n.t(
                            'customizations:extension.ImportReview'
                          )}
                          i18nTypeLabel={i18n.t('shared:Type')}
                          i18nActionText={this.getActionText}
                          onImport={handleImport}
                        />
                      ) : (
                        <Container />
                      )}
                    </>
                  )}
                </Translation>
              );
            }}
          </WithExtensionHelpers>
        )}
      </WithRouteData>
    );
  }
}
