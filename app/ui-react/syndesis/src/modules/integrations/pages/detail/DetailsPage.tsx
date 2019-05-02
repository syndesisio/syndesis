import {
  canActivate,
  canDeactivate,
  WithIntegration,
  WithIntegrationHelpers,
} from '@syndesis/api';
import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  IMenuActions,
  IntegrationDetailBreadcrumb,
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
  IntegrationDetailHistoryListViewItemActions,
  IntegrationDetailInfo,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';

import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import resolvers from '../../../resolvers';
import { IntegrationDetailSteps } from '../../components';
import { TagIntegrationDialogWrapper } from '../../components/TagIntegrationDialogWrapper';
import { IntegrationDetailNavBar } from '../../shared';

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IIntegrationDetailsRouteParams {
  integrationId: string;
}

export interface IIntegrationDetailsRouteState {
  integration?: IIntegrationOverviewWithDraft;
}

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IIntegrationDetailsPageProps {
  error: boolean;
  integration: IIntegrationOverviewWithDraft;
  integrationId: string;
  loading: boolean;
}

export interface IIntegrationDetailsPageState {
  handleAction?: () => void;
  integration?: IIntegrationOverviewWithDraft;
  promptDialogButtonText?: string;
  promptDialogIcon?: ConfirmationIconType;
  promptDialogText?: string;
  promptDialogTitle?: string;
  showActionPromptDialog: boolean;
  showCiCdPromptDialog: boolean;
  targetIntegrationId?: string;
}

interface IPromptActionOptions {
  promptDialogButtonStyle: ConfirmationButtonStyle;
  promptDialogButtonText: string;
  promptDialogIcon: ConfirmationIconType;
  promptDialogText: string;
  promptDialogTitle: string;
  handleAction: () => void;
}

/**
 * This page shows the first, and default, tab of the Integration Detail page.
 *
 * This component expects either an integrationId in the URL,
 * or an integration object set via the state.
 *
 */
export class DetailsPage extends React.Component<
  IIntegrationDetailsPageProps,
  IIntegrationDetailsPageState
> {
  public constructor(props: IIntegrationDetailsPageProps) {
    super(props);
    this.state = {
      showActionPromptDialog: false,
      showCiCdPromptDialog: false,
    };
    this.handleAction = this.handleAction.bind(this);
    this.handleActionCancel = this.handleActionCancel.bind(this);
    this.promptForAction = this.promptForAction.bind(this);
    this.closeCiCdDialog = this.closeCiCdDialog.bind(this);
  }

  public closeCiCdDialog() {
    this.setState({
      showCiCdPromptDialog: false,
    });
  }

  public handleActionCancel() {
    this.setState({
      showActionPromptDialog: false,
    });
  }

  public handleAction() {
    const action = this.state.handleAction;
    this.setState({
      showActionPromptDialog: false,
    });
    if (typeof action === 'function') {
      action.apply(this);
    } else {
      throw Error('Undefined action set for confirmation dialog');
    }
  }

  public promptForAction(options: IPromptActionOptions) {
    this.setState({
      handleAction: options.handleAction,
      promptDialogButtonText: options.promptDialogButtonText,
      promptDialogIcon: options.promptDialogIcon,
      promptDialogText: options.promptDialogText,
      promptDialogTitle: options.promptDialogTitle,
      showActionPromptDialog: true,
    });
  }

  public promptForCiCd(targetIntegrationId: string) {
    this.setState({
      showCiCdPromptDialog: true,
      targetIntegrationId,
    });
  }

  public render() {
    return (
      <WithRouteData<IIntegrationDetailsRouteParams, null>>
        {({ integrationId }) => {
          return (
            <WithIntegrationHelpers>
              {({
                deleteIntegration,
                deployIntegration,
                exportIntegration,
                undeployIntegration,
                tagIntegration,
              }) => {
                return (
                  <WithIntegration integrationId={integrationId}>
                    {({ data, hasData, error }) => (
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={<Loader />}
                        errorChildren={<div>TODO</div>}
                      >
                        {() => (
                          <div>
                            <Translation ns={['integrations', 'shared']}>
                              {t => {
                                const deployments = data.deployments
                                  ? data.deployments
                                  : [];
                                const editAction: IMenuActions = {
                                  href: resolvers.integrations.integration.edit.index(
                                    {
                                      flowId: '0',
                                      integration: data,
                                    }
                                  ),
                                  label: 'Edit',
                                };
                                const startAction: IMenuActions = {
                                  label: 'Start',
                                  onClick: () =>
                                    this.promptForAction({
                                      handleAction: () =>
                                        deployIntegration(
                                          data.id!,
                                          data.version!,
                                          false
                                        ),
                                      promptDialogButtonStyle:
                                        ConfirmationButtonStyle.NORMAL,
                                      promptDialogButtonText: t('shared:Start'),
                                      promptDialogIcon:
                                        ConfirmationIconType.NONE,
                                      promptDialogText: t(
                                        'integrations:publishIntegrationModal',
                                        { name: data.name }
                                      ),
                                      promptDialogTitle: t(
                                        'integrations:publishIntegrationModalTitle'
                                      ),
                                    } as IPromptActionOptions),
                                };
                                const stopAction: IMenuActions = {
                                  label: 'Stop',
                                  onClick: () =>
                                    this.promptForAction({
                                      handleAction: () =>
                                        undeployIntegration(
                                          data.id!,
                                          data.version!
                                        ),
                                      promptDialogButtonStyle:
                                        ConfirmationButtonStyle.NORMAL,
                                      promptDialogButtonText: t('shared:Stop'),
                                      promptDialogIcon:
                                        ConfirmationIconType.NONE,
                                      promptDialogText: t(
                                        'integrations:unpublishIntegrationModal',
                                        { name: data.name }
                                      ),
                                      promptDialogTitle: t(
                                        'integrations:unpublishIntegrationModalTitle'
                                      ),
                                    } as IPromptActionOptions),
                                };
                                const deleteAction: IMenuActions = {
                                  label: 'Delete',
                                  onClick: () =>
                                    this.promptForAction({
                                      handleAction: () =>
                                        deleteIntegration(data.id!),
                                      promptDialogButtonStyle:
                                        ConfirmationButtonStyle.DANGER,
                                      promptDialogButtonText: t(
                                        'shared:Delete'
                                      ),
                                      promptDialogIcon:
                                        ConfirmationIconType.DANGER,
                                      promptDialogText: t(
                                        'integrations:deleteIntegrationModal',
                                        { name: data.name }
                                      ),
                                      promptDialogTitle: t(
                                        'integrations:deleteIntegrationModalTitle'
                                      ),
                                    } as IPromptActionOptions),
                                };
                                const exportAction: IMenuActions = {
                                  label: 'Export',
                                  onClick: () =>
                                    exportIntegration(
                                      data.id!,
                                      `${data.name}-export.zip`
                                    ),
                                };
                                const ciCdAction: IMenuActions = {
                                  label: 'Manage CI/CD',
                                  onClick: () => {
                                    this.promptForCiCd(data.id!);
                                  },
                                };

                                const actions: IMenuActions[] = [];

                                /**
                                 * Array of actions for the breadcrumb.
                                 * One for the buttons, another for the dropdown menu.
                                 */
                                const breadcrumbMenuActions: IMenuActions[] = [];

                                /**
                                 * Array of actions for Draft integrations.
                                 * This is specifically for the inline buttons on the Details tab.
                                 */

                                if (canActivate(data)) {
                                  actions.push(startAction);
                                  breadcrumbMenuActions.push(startAction);
                                }

                                actions.push(editAction);

                                if (canDeactivate(data)) {
                                  actions.push(stopAction);
                                  breadcrumbMenuActions.push(stopAction);
                                }

                                actions.push(deleteAction);
                                breadcrumbMenuActions.push(deleteAction);

                                actions.push(exportAction);

                                actions.push(ciCdAction);
                                breadcrumbMenuActions.push(ciCdAction);

                                return (
                                  <>
                                    {this.state.showCiCdPromptDialog && (
                                      <TagIntegrationDialogWrapper
                                        manageCiCdHref={resolvers.integrations.manageCicd.root()}
                                        tagIntegration={tagIntegration}
                                        targetIntegrationId={
                                          this.state.targetIntegrationId!
                                        }
                                        onSave={this.closeCiCdDialog}
                                        onHide={this.closeCiCdDialog}
                                      />
                                    )}
                                    {this.state.showActionPromptDialog && (
                                      <ConfirmationDialog
                                        buttonStyle={
                                          ConfirmationButtonStyle.NORMAL
                                        }
                                        i18nCancelButtonText={t(
                                          'shared:Cancel'
                                        )}
                                        i18nConfirmButtonText={
                                          this.state.promptDialogButtonText!
                                        }
                                        i18nConfirmationMessage={
                                          this.state.promptDialogText!
                                        }
                                        i18nTitle={
                                          this.state.promptDialogTitle!
                                        }
                                        icon={this.state.promptDialogIcon!}
                                        showDialog={
                                          this.state.showActionPromptDialog!
                                        }
                                        onCancel={this.handleActionCancel}
                                        onConfirm={this.handleAction}
                                      />
                                    )}

                                    <PageTitle
                                      title={t('integrations:detail:pageTitle')}
                                    />

                                    <IntegrationDetailBreadcrumb
                                      editHref={editAction.href}
                                      editLabel={editAction.label}
                                      exportAction={exportAction.onClick}
                                      exportHref={exportAction.href}
                                      exportLabel={exportAction.label}
                                      homeHref={resolvers.dashboard.root()}
                                      i18nHome={t('shared:Home')}
                                      i18nIntegrations={t(
                                        'shared:Integrations'
                                      )}
                                      i18nPageTitle={t(
                                        'integrations:detail:pageTitle'
                                      )}
                                      integrationId={data.id}
                                      integrationsHref={resolvers.integrations.list()}
                                      menuActions={breadcrumbMenuActions}
                                    />

                                    <IntegrationDetailInfo
                                      name={data.name}
                                      version={data.version}
                                    />
                                    <IntegrationDetailNavBar
                                      integration={data}
                                    />
                                    <IntegrationDetailSteps
                                      integration={data}
                                    />
                                    <IntegrationDetailDescription
                                      description={data.description}
                                      i18nNoDescription={t(
                                        'integrations:detail:noDescription'
                                      )}
                                    />
                                    <IntegrationDetailHistoryListView
                                      editHref={editAction.href}
                                      editLabel={editAction.label}
                                      hasHistory={deployments.length > 0}
                                      isDraft={data.isDraft}
                                      i18nTextDraft={t('shared:Draft')}
                                      i18nTextHistory={t(
                                        'integrations:detail:History'
                                      )}
                                      publishAction={
                                        canActivate(data)
                                          ? startAction.onClick
                                          : undefined
                                      }
                                      publishHref={
                                        canActivate(data)
                                          ? startAction.href
                                          : undefined
                                      }
                                      publishLabel={
                                        canActivate(data)
                                          ? t('shared:Publish')
                                          : undefined
                                      }
                                      children={deployments.map(
                                        (deployment, idx) => {
                                          return (
                                            <IntegrationDetailHistoryListViewItem
                                              key={idx}
                                              actions={
                                                <IntegrationDetailHistoryListViewItemActions
                                                  actions={actions}
                                                  integrationId={data.id!}
                                                />
                                              }
                                              currentState={
                                                deployment.currentState!
                                              }
                                              i18nTextLastPublished={t(
                                                'integrations:detail:lastPublished'
                                              )}
                                              i18nTextVersion={t(
                                                'shared:Version'
                                              )}
                                              updatedAt={deployment.updatedAt}
                                              version={deployment.version}
                                            />
                                          );
                                        }
                                      )}
                                    />
                                  </>
                                );
                              }}
                            </Translation>
                          </div>
                        )}
                      </WithLoader>
                    )}
                  </WithIntegration>
                );
              }}
            </WithIntegrationHelpers>
          );
        }}
      </WithRouteData>
    );
  }
}
