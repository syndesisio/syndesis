import {
  canActivate,
  canDeactivate,
  canEdit,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { IntegrationOverview } from '@syndesis/models';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  IIntegrationAction,
} from '@syndesis/ui';
import { WithRouter } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import resolvers from '../resolvers';
import { TagIntegrationDialogWrapper } from './TagIntegrationDialogWrapper';

export interface IWithIntegrationActionsChildrenProps {
  ciCdAction: IIntegrationAction;
  deleteAction: IIntegrationAction;
  editAction: IIntegrationAction;
  exportAction: IIntegrationAction;
  startAction: IIntegrationAction;
  stopAction: IIntegrationAction;
  actions: IIntegrationAction[];
}

export interface IWithIntegrationActionsProps {
  integration: IntegrationOverview;
  postDeleteHref?: H.LocationDescriptorObject;
  children: (props: IWithIntegrationActionsChildrenProps) => any;
}

export interface IWithIntegrationActionsState {
  showActionPromptDialog: boolean;
  showCiCdPromptDialog: boolean;
  targetIntegrationId?: string;
  promptDialogButtonText?: string;
  promptDialogIcon?: ConfirmationIconType;
  promptDialogText?: string;
  promptDialogTitle?: string;
  handleAction?: () => void;
}

export interface IPromptActionOptions {
  promptDialogButtonText: string;
  promptDialogIcon: ConfirmationIconType;
  promptDialogText: string;
  promptDialogTitle: string;
  handleAction: () => void;
}

export class WithIntegrationActions extends React.Component<
  IWithIntegrationActionsProps,
  IWithIntegrationActionsState
> {
  constructor(props: IWithIntegrationActionsProps) {
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
      <WithRouter>
        {({ history }) => {
          return (
            <Translation ns={['integrations', 'shared']}>
              {t => (
                <UIContext.Consumer>
                  {({ pushNotification }) => (
                    <WithIntegrationHelpers>
                      {({
                        deleteIntegration,
                        deployIntegration,
                        exportIntegration,
                        undeployIntegration,
                        tagIntegration,
                      }) => {
                        const editAction: IIntegrationAction = {
                          href: resolvers.integration.edit.entryPoint({
                            flowId: this.props.integration.flows![0].id!,
                            integration: this.props.integration,
                          }),
                          label: 'Edit',
                        };
                        const startAction: IIntegrationAction = {
                          label: 'Start',
                          onClick: () =>
                            this.promptForAction({
                              handleAction: async () => {
                                pushNotification(
                                  i18n.t(
                                    'integrations:PublishingIntegrationMessage'
                                  ),
                                  'info'
                                );
                                try {
                                  await deployIntegration(
                                    this.props.integration.id!,
                                    this.props.integration.version!,
                                    false
                                  );
                                } catch (err) {
                                  pushNotification(
                                    i18n.t(
                                      'integrations:PublishingIntegrationFailedMessage',
                                      {
                                        error:
                                          err.errorMessage ||
                                          err.message ||
                                          err,
                                      }
                                    ),
                                    'warning'
                                  );
                                }
                              },
                              promptDialogButtonStyle:
                                ConfirmationButtonStyle.NORMAL,
                              promptDialogButtonText: t('shared:Start'),
                              promptDialogIcon: ConfirmationIconType.NONE,
                              promptDialogText: t(
                                'integrations:publishIntegrationModal',
                                { name: this.props.integration.name }
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
                              handleAction: async () => {
                                pushNotification(
                                  i18n.t(
                                    'integrations:UnpublishingIntegrationMessage'
                                  ),
                                  'info'
                                );
                                try {
                                  undeployIntegration(
                                    this.props.integration.id!,
                                    this.props.integration.version!
                                  );
                                } catch (err) {
                                  pushNotification(
                                    i18n.t(
                                      'integrations:UnpublishingIntegrationFailedMessage',
                                      {
                                        error:
                                          err.errorMessage ||
                                          err.message ||
                                          err,
                                      }
                                    ),
                                    'warning'
                                  );
                                }
                              },
                              promptDialogButtonStyle:
                                ConfirmationButtonStyle.NORMAL,
                              promptDialogButtonText: t('shared:Stop'),
                              promptDialogIcon: ConfirmationIconType.NONE,
                              promptDialogText: t(
                                'integrations:unpublishIntegrationModal',
                                { name: this.props.integration.name }
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
                              handleAction: async () => {
                                pushNotification(
                                  i18n.t(
                                    'integrations:DeletingIntegrationMessage'
                                  ),
                                  'info'
                                );
                                try {
                                  await deleteIntegration(
                                    this.props.integration.id!
                                  );

                                  // redirect if requested
                                  if (this.props.postDeleteHref) {
                                    history.push(this.props.postDeleteHref);
                                  }
                                } catch (err) {
                                  pushNotification(
                                    i18n.t(
                                      'integrations:DeletingIntegrationFailedMessage',
                                      {
                                        error:
                                          err.errorMessage ||
                                          err.message ||
                                          err,
                                      }
                                    ),
                                    'warning'
                                  );
                                }
                              },
                              promptDialogButtonStyle:
                                ConfirmationButtonStyle.DANGER,
                              promptDialogButtonText: t('shared:Delete'),
                              promptDialogIcon: ConfirmationIconType.DANGER,
                              promptDialogText: t(
                                'integrations:deleteIntegrationModal',
                                { name: this.props.integration.name }
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
                              this.props.integration.id!,
                              `${this.props.integration.name}-export.zip`
                            ),
                        };
                        const ciCdAction: IIntegrationAction = {
                          label: 'Manage CI/CD',
                          onClick: () => {
                            this.promptForCiCd(this.props.integration.id!);
                          },
                        };

                        const actions: IIntegrationAction[] = [];
                        if (canEdit(this.props.integration)) {
                          actions.push(editAction);
                        }
                        if (canActivate(this.props.integration)) {
                          actions.push(startAction);
                        }
                        if (canDeactivate(this.props.integration)) {
                          actions.push(stopAction);
                        }
                        actions.push(deleteAction);
                        actions.push(exportAction);
                        actions.push(ciCdAction);
                        return (
                          <>
                            {this.state.showCiCdPromptDialog && (
                              <TagIntegrationDialogWrapper
                                manageCiCdHref={resolvers.manageCicd.root()}
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
                                buttonStyle={ConfirmationButtonStyle.NORMAL}
                                i18nCancelButtonText={t('shared:Cancel')}
                                i18nConfirmButtonText={
                                  this.state.promptDialogButtonText!
                                }
                                i18nConfirmationMessage={
                                  this.state.promptDialogText!
                                }
                                i18nTitle={this.state.promptDialogTitle!}
                                icon={this.state.promptDialogIcon!}
                                showDialog={this.state.showActionPromptDialog}
                                onCancel={this.handleActionCancel}
                                onConfirm={this.handleAction}
                              />
                            )}
                            {this.props.children({
                              actions,
                              ciCdAction,
                              deleteAction,
                              editAction,
                              exportAction,
                              startAction,
                              stopAction,
                            })}
                          </>
                        );
                      }}
                    </WithIntegrationHelpers>
                  )}
                </UIContext.Consumer>
              )}
            </Translation>
          );
        }}
      </WithRouter>
    );
  }
}
