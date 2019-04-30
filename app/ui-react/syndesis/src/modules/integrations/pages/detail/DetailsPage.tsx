import {
  canActivate,
  canDeactivate,
  canEdit,
  getSteps,
  WithIntegration,
  WithIntegrationHelpers,
} from '@syndesis/api';
import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  IIntegrationAction,
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailInfo,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import {
  IntegrationDetailHistory,
  IntegrationDetailSteps,
} from '../../components';
import resolvers from '../../resolvers';
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
  showPromptDialog?: boolean;
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
      showPromptDialog: false,
    };
    this.handleAction = this.handleAction.bind(this);
    this.handleActionCancel = this.handleActionCancel.bind(this);
    this.promptForAction = this.promptForAction.bind(this);
  }

  public handleActionCancel() {
    this.setState({
      showPromptDialog: false,
    });
  }

  public handleAction() {
    const action = this.state.handleAction;
    this.setState({
      showPromptDialog: false,
    });
    if (typeof action === 'function') {
      action.apply(this);
    } else {
      throw Error('Undefined action set for confirmation dialog');
    }
  }

  public promptForAction(options: IPromptActionOptions) {
    this.setState({
      ...options,
      showPromptDialog: true,
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
                                const editAction: IIntegrationAction = {
                                  href: resolvers.integration.edit.index({
                                    flowId: '0',
                                    integration: data,
                                  }),
                                  label: 'Edit',
                                };
                                const startAction: IIntegrationAction = {
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
                                const stopAction: IIntegrationAction = {
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
                                const deleteAction: IIntegrationAction = {
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
                                const exportAction: IIntegrationAction = {
                                  label: 'Export',
                                  onClick: () =>
                                    exportIntegration(
                                      data.id!,
                                      `${data.name}-export.zip`
                                    ),
                                };

                                const actions: IIntegrationAction[] = [];
                                if (canEdit(data)) {
                                  actions.push(editAction);
                                }
                                if (canActivate(data)) {
                                  actions.push(startAction);
                                }
                                if (canDeactivate(data)) {
                                  actions.push(stopAction);
                                }
                                actions.push(deleteAction);
                                actions.push(exportAction);

                                return (
                                  <>
                                    <ConfirmationDialog
                                      buttonStyle={
                                        ConfirmationButtonStyle.NORMAL
                                      }
                                      i18nCancelButtonText={t('shared:Cancel')}
                                      i18nConfirmButtonText={
                                        this.state.promptDialogButtonText!
                                      }
                                      i18nConfirmationMessage={
                                        this.state.promptDialogText!
                                      }
                                      i18nTitle={this.state.promptDialogTitle!}
                                      icon={this.state.promptDialogIcon!}
                                      showDialog={this.state.showPromptDialog!}
                                      onCancel={this.handleActionCancel}
                                      onConfirm={this.handleAction}
                                    />
                                    <IntegrationDetailInfo
                                      name={data.name}
                                      version={data.version}
                                    />
                                    <IntegrationDetailNavBar
                                      integration={data}
                                    />
                                    <IntegrationDetailSteps
                                      steps={getSteps(data, data.flows![0].id!)}
                                    />
                                    <IntegrationDetailDescription
                                      description={data.description}
                                      i18nNoDescription={t(
                                        'integrations:detail:noDescription'
                                      )}
                                    />
                                    <IntegrationDetailHistoryListView
                                      hasHistory={deployments.length > 0}
                                      isDraft={data.isDraft}
                                      i18nTextBtnEdit={t('shared:Edit')}
                                      i18nTextBtnPublish={t('shared:Publish')}
                                      i18nTextDraft={t('shared:Draft')}
                                      i18nTextHistory={t(
                                        'integrations:detail:History'
                                      )}
                                      children={
                                        <IntegrationDetailHistory
                                          actions={actions}
                                          deployments={deployments}
                                          integrationId={data.id!}
                                          updatedAt={data.updatedAt!}
                                          version={data.version!}
                                        />
                                      }
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
