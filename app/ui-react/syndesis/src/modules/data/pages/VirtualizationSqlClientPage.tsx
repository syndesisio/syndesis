import { WithViewEditorStates, WithVirtualizationHelpers } from '@syndesis/api';
import { RestDataService, ViewEditorState } from '@syndesis/models';
import { PageSection, ViewHeader, ViewHeaderBreadcrumb } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
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
export class VirtualizationSqlClientPage extends React.Component<
  IVirtualizationSqlClientPageRouteState
> {
  public handleSubmit() {
    // TODO: finish form handling
  }

  public render() {
    return (
      <Translation ns={['data', 'shared']}>
        {t => (
          <AppContext.Consumer>
            {({ config }) => {
              return (
                <UIContext.Consumer>
                  {({ pushNotification }) => {
                    return (
                      <WithRouteData<
                        IVirtualizationSqlClientPageRouteParams,
                        IVirtualizationSqlClientPageRouteState
                      >>
                        {(
                          { virtualizationId },
                          { virtualization },
                          { history }
                        ) => {
                          return (
                            <WithVirtualizationHelpers>
                              {({
                                deleteVirtualization,
                                publishVirtualization,
                                unpublishServiceVdb,
                              }) => {
                                const publishingDetails = getPublishingDetails(
                                  config.consoleUrl,
                                  virtualization
                                );
                                const handleDelete = async (
                                  virtualizationId: string
                                ) => {
                                  try {
                                    await deleteVirtualization(
                                      virtualizationId
                                    );
                                    pushNotification(
                                      t(
                                        'virtualization.deleteVirtualizationSuccess',
                                        { name: virtualizationId }
                                      ),
                                      'success'
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
                                          name: virtualizationId,
                                        }
                                      ),
                                      'error'
                                    );
                                  }
                                };
                                const handlePublish = async (
                                  virtualizationId: string,
                                  hasViews: boolean
                                ) => {
                                  if (hasViews) {
                                    try {
                                      await publishVirtualization(
                                        virtualizationId
                                      );

                                      pushNotification(
                                        t(
                                          'virtualization.publishVirtualizationSuccess',
                                          { name: virtualizationId }
                                        ),
                                        'success'
                                      );
                                    } catch (error) {
                                      const details = error.error
                                        ? error.error
                                        : '';
                                      pushNotification(
                                        t(
                                          'virtualization.publishVirtualizationFailed',
                                          { name: virtualizationId, details }
                                        ),
                                        'error'
                                      );
                                    }
                                  } else {
                                    pushNotification(
                                      t(
                                        'virtualization.publishVirtualizationNoViews',
                                        { name: virtualizationId }
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
                                        virtualization.keng__id
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
                                          name: virtualization.keng__id,
                                        }
                                      )}
                                      i18nDeleteModalTitle={t(
                                        'virtualization.deleteModalTitle'
                                      )}
                                      i18nDraft={t('shared:Draft')}
                                      i18nEdit={t('shared:Edit')}
                                      i18nEditTip={t(
                                        'virtualization.editDataVirtualizationTip'
                                      )}
                                      i18nError={t('shared:Error')}
                                      /* TD-636: Commented out for TP
                                i18nExport={t('shared:Export')} */
                                      i18nPublish={t('shared:Publish')}
                                      i18nPublished={t(
                                        'virtualization.publishedDataVirtualization'
                                      )}
                                      i18nUnpublish={t('shared:Unpublish')}
                                      i18nUnpublishModalMessage={t(
                                        'virtualization.unpublishModalMessage',
                                        {
                                          name: virtualization.keng__id,
                                        }
                                      )}
                                      i18nUnpublishModalTitle={t(
                                        'virtualization.unpublishModalTitle'
                                      )}
                                      onDelete={handleDelete}
                                      /* TD-636: Commented out for TP
                                onExport={
                                  this
                                    .handleExportVirtualization
                                } */
                                      onUnpublish={handleUnpublish}
                                      onPublish={handlePublish}
                                      publishingLogUrl={
                                        publishingDetails.logUrl
                                      }
                                      publishingCurrentStep={
                                        publishingDetails.stepNumber
                                      }
                                      publishingTotalSteps={
                                        publishingDetails.stepTotal
                                      }
                                      publishingStepText={
                                        publishingDetails.stepText
                                      }
                                      i18nPublishInProgress={t(
                                        'virtualization.publishInProgress'
                                      )}
                                      i18nPublishLogUrlText={t(
                                        'shared:viewLogs'
                                      )}
                                      serviceVdbName={
                                        virtualization.serviceVdbName
                                      }
                                      virtualizationViewNames={
                                        virtualization.serviceViewDefinitions
                                      }
                                    />
                                    <ViewHeader
                                      i18nTitle={virtualization.keng__id}
                                      i18nDescription={
                                        virtualization.tko__description
                                      }
                                    />
                                    <PageSection
                                      variant={'light'}
                                      noPadding={true}
                                    >
                                      <VirtualizationNavBar
                                        virtualization={virtualization}
                                      />
                                    </PageSection>
                                    <WithViewEditorStates
                                      idPattern={
                                        virtualization.serviceVdbName + '*'
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
                              }}
                            </WithVirtualizationHelpers>
                          );
                        }}
                      </WithRouteData>
                    );
                  }}
                </UIContext.Consumer>
              );
            }}
          </AppContext.Consumer>
        )}
      </Translation>
    );
  }
}
