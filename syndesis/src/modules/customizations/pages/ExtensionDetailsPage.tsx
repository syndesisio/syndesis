import { WithExtension } from '@syndesis/api';
import { Action, Extension } from '@syndesis/models';
import {
  Breadcrumb,
  ExtensionDetail,
  ExtensionOverview,
  ExtensionSupports,
  IAction,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import i18n from '../../../i18n';
import resolvers from '../../resolvers';
import ExtensionIntegrations from '../components/ExtensionIntegrations';

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
      return i18n.t('customizations:extension.StepExtension');
    }

    if ('Connectors' === type) {
      return i18n.t('customizations:extension.ConnectorExtension');
    }

    if ('Libraries' === type) {
      return i18n.t('customizations:extension.LibraryExtension');
    }

    return i18n.t('customizations:extension.unknownExtensionType');
  }

  public handleDelete(): void {
    alert('TODO: Delete extension');
  }

  public handleIntegrationSelected(integrationId: string) {
    alert("TODO: Show integration '" + integrationId + "'");
  }

  public handleUpdate(): void {
    alert('TODO: Update extension');
  }

  public render() {
    return (
      <WithRouteData<IExtensionDetailRouteParams, IExtensionDetailRouteState>>
        {({ extensionId }, { extension } = {}) => {
          return (
            <WithExtension extensionId={extensionId} initialValue={extension}>
              {({ data, hasData, error }) => (
                <WithLoader
                  error={error}
                  loading={!hasData}
                  loaderChildren={<Loader />}
                  errorChildren={<div>TODO</div>}
                >
                  {() => (
                    <Translation ns={['customizations', 'shared']}>
                      {t => (
                        <>
                          <Breadcrumb>
                            <Link to={resolvers.dashboard.root()}>
                              {t('shared:Home')}
                            </Link>
                            <Link to={resolvers.customizations.root()}>
                              {t('shared:Customizations')}
                            </Link>
                            <Link
                              to={resolvers.customizations.extensions.list()}
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
                            i18nDelete={t('shared:Delete')}
                            i18nDeleteTip={t('extension.deleteExtensionTip')}
                            i18nIdMessage={t('extension.idMessage', {
                              id: data.extensionId!,
                            })}
                            i18nOverviewSectionTitle={t(
                              'extension.overviewSectionTitle'
                            )}
                            i18nSupportsSectionTitle={
                              data.extensionType === 'Steps'
                                ? t('extension.supportedStepsSectionTitle')
                                : data.extensionType === 'Connectors'
                                ? t('extension.supportedConnectorsSectionTitle')
                                : t('extension.supportedLibrariesSectionTitle')
                            }
                            i18nUpdate={t('shared:Update')}
                            i18nUpdateTip={t('extension.updateExtensionTip')}
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
                                  this.handleIntegrationSelected
                                }
                              />
                            }
                            onDelete={this.handleDelete}
                            onUpdate={this.handleUpdate}
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
      </WithRouteData>
    );
  }
}
