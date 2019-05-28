import { WithIntegrationHelpers } from '@syndesis/api';
import { Breadcrumb, ImportPageUI } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import i18n from '../../../../i18n';
import { PageTitle } from '../../../../shared';
import resolvers from '../../resolvers';

export interface IImportPageState {
  disableDropzone: boolean;
  uploadFailedMessages?: string[];
  uploadSuccessMessages?: string[];
}
export class ImportPage extends React.Component<{}, IImportPageState> {
  constructor(props: {}) {
    super(props);
    this.state = {
      disableDropzone: false,
    };
  }
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <WithIntegrationHelpers>
            {({ importIntegration }) => {
              const handleUploadAccepted = async (files: File[]) => {
                let uploadFails: string[] = [];
                let uploadWins: string[] = [];
                this.setState({
                  disableDropzone: true,
                  uploadFailedMessages: uploadFails,
                  uploadSuccessMessages: uploadWins,
                });
                for (const file of files) {
                  try {
                    await importIntegration(file);
                    uploadWins = uploadWins.concat([
                      i18n.t('integrations:ImportUploadSuccessMessage', {
                        fileName: file.name,
                      }),
                    ]);
                  } catch (err) {
                    uploadFails = uploadFails.concat([
                      i18n.t('integrations:ImportUploadFailedMessage', {
                        fileName: file.name,
                      }),
                    ]);
                  }
                  this.setState({
                    uploadFailedMessages: uploadFails,
                    uploadSuccessMessages: uploadWins,
                  });
                }
                this.setState({
                  disableDropzone: false,
                });
              };
              const handleUploadRejected = (failed: string) => {
                return i18n.t('integrations:ImportUploadFailedAlertMessage', {
                  fileName: failed,
                });
              };
              return (
                <>
                  <PageTitle title={t('shared:Import')} />
                  <Breadcrumb>
                    <Link
                      data-testid={'import-page-integrations-link'}
                      to={resolvers.list()}
                    >
                      {t('shared:Integrations')}
                    </Link>
                    <span>{t('integrations:ImportIntegration')}</span>
                  </Breadcrumb>
                  <ImportPageUI
                    i18nPageTitle={t('integrations:ImportIntegration')}
                    i18nPageDescription={t(
                      'integrations:ImportIntegrationDescription'
                    )}
                    i18nNoFileSelectedMessage={t(
                      'integrations:ImportNoFileSelectedMessage'
                    )}
                    i18nSelectedFileLabel={t(
                      'integrations:ImportSelectedFileLabel'
                    )}
                    i18nInstructions={t(
                      'integrations:ImportIntegrationInstructions'
                    )}
                    i18nHelpMessage={t('integrations:ImportHelpMessage')}
                    i18nUploadSuccessMessages={this.state.uploadSuccessMessages}
                    i18nUploadFailedMessages={this.state.uploadFailedMessages}
                    onUploadRejected={handleUploadRejected}
                    onUploadAccepted={handleUploadAccepted}
                  />
                </>
              );
            }}
          </WithIntegrationHelpers>
        )}
      </Translation>
    );
  }
}
