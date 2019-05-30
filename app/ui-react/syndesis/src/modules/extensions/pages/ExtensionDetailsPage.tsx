import { WithExtension, WithExtensionHelpers } from '@syndesis/api';
import { Action, Extension } from '@syndesis/models';
import {
  Breadcrumb,
  ExtensionDetail,
  ExtensionOverview,
  ExtensionSupports,
  IAction,
  PageLoader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import resolvers from '../../resolvers';
import ExtensionIntegrations from '../shared/ExtensionIntegrations';

/**
 * @param extensionId - the ID of the extension whose details are being shown by this page.
 */
export interface IExtensionDetailRouteParams {
  extensionId: string;
}

/**
 * @param extension - the extension whose details are being shown by this page. If
 * exists, it must equal to the [extensionId]{@link IExtensionDetailRouteParams#extensionId}.
 * This is used to immediately show the details to the user, without
 * any loader; the backend will be called nonetheless to ensure that we are
 * working with the latest data available. This will be used when navigating from the
 * [ExtensionPage]{@link ExtensionsPage}.
 */
export interface IExtensionDetailRouteState {
  extension?: Extension;
}

export default class ExtensionDetailsPage extends React.Component {
  public getTypeMessage(type: string): string {
    if ('Steps' === type) {
      return i18n.t('extensions:extension.StepExtension');
    }

    if ('Connectors' === type) {
      return i18n.t('extensions:extension.ConnectorExtension');
    }

    if ('Libraries' === type) {
      return i18n.t('extensions:extension.LibraryExtension');
    }

    return i18n.t('extensions:extension.unknownExtensionType');
  }

  public render() {
    return (
      <WithRouteData<IExtensionDetailRouteParams, IExtensionDetailRouteState>>
        {({ extensionId }, { extension }, { history }) => {
          const handleSelectIntegration = (selectedIntegrationId: string) => {
            // redirect to the integration detail page
            history.push(
              resolvers.integrations.integration.details({
                integrationId: selectedIntegrationId,
              })
            );
          };
          return (
            <WithExtensionHelpers>
              {({ deleteExtension }) => {
                const handleDelete = async () => {
                  await deleteExtension(extensionId);
                  // TODO: post toast notification
                  // display extensions list page
                  history.push(resolvers.extensions.list());
                };
                return (
                  <WithExtension
                    extensionId={extensionId}
                    initialValue={extension}
                  >
                    {({ data, hasData, error }) => (
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={<PageLoader />}
                        errorChildren={<ApiError />}
                      >
                        {() => (
                          <Translation ns={['extensions', 'shared']}>
                            {t => (
                              <>
                                <Breadcrumb>
                                  <Link
                                    data-testid={
                                      'extension-details-page-home-link'
                                    }
                                    to={resolvers.dashboard.root()}
                                  >
                                    {t('shared:Home')}
                                  </Link>
                                  <Link
                                    data-testid={
                                      'extension-details-page-extensions-link'
                                    }
                                    to={resolvers.extensions.list()}
                                  >
                                    {t('shared:Extensions')}
                                  </Link>
                                  <span>
                                    {t('extension.extensionDetailPageTitle')}
                                  </span>
                                </Breadcrumb>
                                <ExtensionDetail
                                  extensionName={data.name}
                                  // TODO: Schema is currently wrong as it has 'uses` as an OptionalInt. Remove cast when schema is fixed.
                                  extensionUses={data.uses as number}
                                  i18nCancelText={t('shared:Cancel')}
                                  i18nDelete={t('shared:Delete')}
                                  i18nDeleteModalMessage={t(
                                    'extension.deleteModalMessage',
                                    { name: data.name }
                                  )}
                                  i18nDeleteModalTitle={t(
                                    'extension.deleteModalTitle'
                                  )}
                                  i18nDeleteTip={t(
                                    'extension.deleteExtensionTip'
                                  )}
                                  i18nIdMessage={t('extension.idMessage', {
                                    id: data.extensionId!,
                                  })}
                                  i18nOverviewSectionTitle={t(
                                    'extension.overviewSectionTitle'
                                  )}
                                  i18nSupportsSectionTitle={
                                    data.extensionType === 'Steps'
                                      ? t(
                                          'extension.supportedStepsSectionTitle'
                                        )
                                      : data.extensionType === 'Connectors'
                                      ? t(
                                          'extension.supportedConnectorsSectionTitle'
                                        )
                                      : t(
                                          'extension.supportedLibrariesSectionTitle'
                                        )
                                  }
                                  i18nUpdate={t('shared:Update')}
                                  i18nUpdateTip={t(
                                    'extension.updateExtensionTip'
                                  )}
                                  i18nUsageSectionTitle={t(
                                    'extension.usageSectionTitle'
                                  )}
                                  integrationsSection={
                                    <ExtensionIntegrations
                                      extensionId={data.id!}
                                      uses={
                                        // TODO: Schema is currently wrong as it has 'uses` as an OptionalInt. Remove cast when schema is fixed.
                                        data.uses as number
                                      }
                                      onSelectIntegration={
                                        handleSelectIntegration
                                      }
                                    />
                                  }
                                  linkUpdateExtension={resolvers.extensions.extension.update(
                                    { extension: data }
                                  )}
                                  onDelete={handleDelete}
                                  overviewSection={
                                    <ExtensionOverview
                                      extensionDescription={data.description}
                                      extensionName={data.name}
                                      i18nDescription={t('shared:Description')}
                                      i18nLastUpdate={t('extension.LastUpdate')}
                                      i18nLastUpdateDate={
                                        data.lastUpdated
                                          ? new Date(
                                              data.lastUpdated
                                            ).toLocaleString()
                                          : ''
                                      }
                                      i18nName={t('shared:Name')}
                                      i18nType={t('shared:Type')}
                                      i18nTypeMessage={this.getTypeMessage(
                                        data.extensionType
                                      )}
                                    />
                                  }
                                  supportsSection={
                                    <ExtensionSupports
                                      extensionActions={data.actions.map(
                                        (action: Action) =>
                                          ({
                                            description: action.description,
                                            name: action.name,
                                          } as IAction)
                                      )}
                                    />
                                  }
                                />
                              </>
                            )}
                          </Translation>
                        )}
                      </WithLoader>
                    )}
                  </WithExtension>
                );
              }}
            </WithExtensionHelpers>
          );
        }}
      </WithRouteData>
    );
  }
}
