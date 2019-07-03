import { useVirtualizationHelpers, WithViewEditorStates } from '@syndesis/api';
import { RestDataService, ViewEditorState } from '@syndesis/models';
import { PageSection, ViewHeader, ViewHeaderBreadcrumb } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import { useContext } from 'react';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { AppContext, UIContext } from '../../../app';
import resolvers from '../../resolvers';
import {
  VirtualizationNavBar,
  WithVirtualizationSqlClientForm,
} from '../shared';
import { getPreviewVdbName } from '../shared/VirtualizationUtils';
import { getPublishingDetails } from '../shared/VirtualizationUtils';

/**
 * @param virtualizationId - the ID of the virtualization shown by this page.
 */
export interface IVirtualizationSqlClientPageRouteParams {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * @param virtualization - the virtualization being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationSqlClientPageRouteParams#virtualizationId}.
 */
export interface IVirtualizationSqlClientPageRouteState {
  virtualization: RestDataService;
}

/**
 * Page displays virtualization views and allows user run test queries against the views.
 */
export const VirtualizationSqlClientPage: React.FunctionComponent = () => {

  const { t } = useTranslation(['data', 'shared']);
  const { params, state, history } = useRouteData<
    IVirtualizationSqlClientPageRouteParams,
    IVirtualizationSqlClientPageRouteState
  >();
  const appContext = React.useContext(AppContext);
  const { pushNotification } = useContext(UIContext);
  const { deleteVirtualization, publishVirtualization, unpublishServiceVdb} = useVirtualizationHelpers();
  
  const virtualization = state.virtualization;
  const publishingDetails = getPublishingDetails(
    appContext.config.consoleUrl,
    state.virtualization
  );

  const handleDelete = async (
    pVirtualizationId: string
  ) => {
    try {
      await deleteVirtualization(
        pVirtualizationId
      );
      pushNotification(
        t(
          'virtualization.deleteVirtualizationSuccess',
          { name: params.virtualizationId }
        ),
        'success'
      );
      // On successful delete, redirect to virtualizations page
      // TODO: Handle publish/unpublish on current page
      history.push(
        resolvers.data.virtualizations.list()
      );
    } catch (error) {
      const details = error.message
        ? error.message
        : '';
      pushNotification(
        t(
          'virtualization.deleteVirtualizationFailed',
          {
            details,
            name: params.virtualizationId,
          }
        ),
        'error'
      );
    }
  };

  const handlePublish = async (
    pVirtualizationId: string,
    hasViews: boolean
  ) => {
    if (hasViews) {
      try {
        await publishVirtualization(
          pVirtualizationId
        );

        pushNotification(
          t(
            'virtualization.publishVirtualizationSuccess',
            { name: params.virtualizationId }
          ),
          'success'
        );
        // On publish, redirect to virtualizations page
        // TODO: Handle publish/unpublish on current page
        history.push(
          resolvers.data.virtualizations.list()
        );
      } catch (error) {
        const details = error.error
          ? error.error
          : '';
        pushNotification(
          t(
            'virtualization.publishVirtualizationFailed',
            { name: params.virtualizationId, details }
          ),
          'error'
        );
      }
    } else {
      pushNotification(
        t(
          'virtualization.publishVirtualizationNoViews',
          { name: params.virtualizationId }
        ),
        'error'
      );
    }
  };
  
  const handleUnpublish = async (
    serviceVdbName: string
  ) => {
    try {
      await unpublishServiceVdb(serviceVdbName);

      pushNotification(
        t(
          'virtualization.unpublishVirtualizationSuccess',
          { name: serviceVdbName }
        ),
        'success'
      );
      // On successful delete, redirect to virtualizations page
      history.push(
        resolvers.data.virtualizations.list()
      );
    } catch (error) {
      const details = error.message
        ? error.message
        : '';
      pushNotification(
        t('virtualization.unpublishFailed', {
          details,
          name: serviceVdbName,
        }),
        'error'
      );
    }
  };

  return (
    <>
      <ViewHeaderBreadcrumb
        currentPublishedState={
          publishingDetails.state
        }
        virtualizationName={
          state.virtualization.keng__id
        }
        dashboardHref={resolvers.dashboard.root()}
        dashboardString={t('shared:Home')}
        dataHref={resolvers.data.root()}
        dataString={t('shared:Virtualizations')}
        i18nViews={t('virtualization.views')}
        i18nCancelText={t('shared:Cancel')}
        i18nDelete={t('shared:Delete')}
        i18nDeleteModalMessage={t(
          'virtualization.deleteModalMessage',
          {
            name: state.virtualization.keng__id,
          }
        )}
        i18nDeleteModalTitle={t(
          'virtualization.deleteModalTitle'
        )}
        i18nPublish={t('shared:Publish')}
        i18nUnpublish={t('shared:Unpublish')}
        i18nUnpublishModalMessage={t(
          'virtualization.unpublishModalMessage',
          {
            name: state.virtualization.keng__id,
          }
        )}
        i18nUnpublishModalTitle={t(
          'virtualization.unpublishModalTitle'
        )}
        onDelete={handleDelete}
        /* TD-636: Commented out for TP
           onExport={this.handleExportVirtualization} */
        onUnpublish={handleUnpublish}
        onPublish={handlePublish}
        serviceVdbName={
          state.virtualization.serviceVdbName
        }
        hasViews={
          state.virtualization.serviceViewDefinitions &&
          state.virtualization.serviceViewDefinitions
            .length > 0
        }
      />
      <ViewHeader
        i18nTitle={state.virtualization.keng__id}
        i18nDescription={
          state.virtualization.tko__description
        }
      />
      <PageSection
        variant={'light'}
        noPadding={true}
      >
        <VirtualizationNavBar
          virtualization={state.virtualization}
        />
      </PageSection>
      <WithViewEditorStates
        idPattern={
          state.virtualization.serviceVdbName + '*'
        }
      >
        {({ data, hasData, error }) => (
          <WithVirtualizationSqlClientForm
            views={data.map(
              (editorState: ViewEditorState) =>
                editorState.viewDefinition
            )}
            targetVdb={getPreviewVdbName()}
            linkCreateView={resolvers.data.virtualizations.create()}
            linkImportViews={resolvers.data.virtualizations.views.importSource.selectConnection(
              { virtualization }
            )}
          >
            {({
              form,
              submitForm,
              isSubmitting,
            }) => <></>}
          </WithVirtualizationSqlClientForm>
        )}
      </WithViewEditorStates>
    </>
  );

}
