import { useExtension, useExtensionHelpers } from '@syndesis/api';
import { Action, Extension } from '@syndesis/models';
import {
  Breadcrumb,
  ExtensionDetail,
  ExtensionOverview,
  ExtensionSupports,
  IAction,
  PageLoader,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../app';
import { ApiError } from '../../../shared';
import resolvers from '../../resolvers';
import { ExtensionIntegrations } from '../shared/ExtensionIntegrations';

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

export const ExtensionDetailsPage: React.FunctionComponent = () => {
  const { params, state, history } = useRouteData<
    IExtensionDetailRouteParams,
    IExtensionDetailRouteState
  >();
  const { pushNotification } = React.useContext(UIContext);
  const { t } = useTranslation(['extensions', 'shared']);
  const { resource: extension, error, hasData } = useExtension(
    params.extensionId,
    state.extension
  );
  const { deleteExtension } = useExtensionHelpers();

  const getTypeMessage = (type: string): string => {
    if ('Steps' === type) {
      return t('extension.StepExtension');
    }

    if ('Connectors' === type) {
      return t('extension.ConnectorExtension');
    }

    if ('Libraries' === type) {
      return t('extension.LibraryExtension');
    }

    return t('extension.unknownExtensionType');
  };

  const handleDelete = async () => {
    try {
      await deleteExtension(params.extensionId);
      history.push(resolvers.extensions.list());
      pushNotification(t('extension.extensionDeletedMessage'), 'success');
    } catch {
      pushNotification(
        t('extension.errorDeletingExtension', {
          extensionId: params.extensionId,
        }),
        'error'
      );
    }
  };

  const handleSelectIntegration = (selectedIntegrationId: string) => {
    // redirect to the integration detail page
    history.push(
      resolvers.integrations.integration.details({
        integrationId: selectedIntegrationId,
      })
    );
  };

  return (
    <WithLoader
      error={error !== false}
      loading={!hasData}
      loaderChildren={<PageLoader />}
      errorChildren={<ApiError error={error as Error} />}
    >
      {() => (
        <>
          <Breadcrumb>
            <Link
              data-testid={'extension-details-page-home-link'}
              to={resolvers.dashboard.root()}
            >
              {t('shared:Home')}
            </Link>
            <Link
              data-testid={'extension-details-page-extensions-link'}
              to={resolvers.extensions.list()}
            >
              {t('shared:Extensions')}
            </Link>
            <span>{t('extension.extensionDetailPageTitle')}</span>
          </Breadcrumb>
          <ExtensionDetail
            extensionName={extension.name}
            // TODO: Schema is currently wrong as it has 'uses` as an OptionalInt. Remove cast when schema is fixed.
            extensionUses={extension.uses as number}
            i18nCancelText={t('shared:Cancel')}
            i18nDelete={t('shared:Delete')}
            i18nDeleteModalMessage={t('extension.deleteModalMessage', {
              name: extension.name,
            })}
            i18nDeleteModalTitle={t('extension.deleteModalTitle')}
            i18nDeleteTip={t('extension.deleteExtensionTip')}
            i18nIdMessage={t('extension.idMessage', {
              id: extension.extensionId!,
            })}
            i18nOverviewSectionTitle={t('extension.overviewSectionTitle')}
            i18nSupportsSectionTitle={
              extension.extensionType === 'Steps'
                ? t('extension.supportedStepsSectionTitle')
                : extension.extensionType === 'Connectors'
                ? t('extension.supportedConnectorsSectionTitle')
                : t('extension.supportedLibrariesSectionTitle')
            }
            i18nUpdate={t('shared:Update')}
            i18nUpdateTip={t('extension.updateExtensionTip')}
            i18nUsageSectionTitle={t('extension.usageSectionTitle')}
            integrationsSection={
              <ExtensionIntegrations
                extensionId={extension.id!}
                uses={
                  // TODO: Schema is currently wrong as it has 'uses` as an OptionalInt. Remove cast when schema is fixed.
                  extension.uses as number
                }
                onSelectIntegration={handleSelectIntegration}
              />
            }
            linkUpdateExtension={resolvers.extensions.extension.update({
              extension,
            })}
            onDelete={handleDelete}
            overviewSection={
              <ExtensionOverview
                extensionDescription={extension.description}
                extensionName={extension.name}
                i18nDescription={t('shared:Description')}
                i18nLastUpdate={t('extension.LastUpdate')}
                i18nLastUpdateDate={
                  extension.lastUpdated
                    ? new Date(extension.lastUpdated).toLocaleString()
                    : ''
                }
                i18nName={t('shared:Name')}
                i18nType={t('shared:Type')}
                i18nTypeMessage={getTypeMessage(extension.extensionType)}
              />
            }
            supportsSection={
              <ExtensionSupports
                extensionActions={(extension.actions || []).map(
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
    </WithLoader>
  );
};
