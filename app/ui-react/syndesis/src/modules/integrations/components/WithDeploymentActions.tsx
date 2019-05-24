import { WithIntegrationHelpers } from '@syndesis/api';
import { IntegrationDeployment } from '@syndesis/models';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  IMenuActions,
} from '@syndesis/ui';
import * as React from 'react';

import { Translation } from 'react-i18next';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { IPromptActionOptions } from './WithIntegrationActions';

export interface IWithDeploymentActionsChildrenProps {
  startDeploymentAction: IMenuActions;
  stopDeploymentAction: IMenuActions;
  replaceDraftAction: IMenuActions;
}

export interface IWithDeploymentActionsProps {
  integrationName: string;
  integrationId: string;
  deployment: IntegrationDeployment;
  children: (props: IWithDeploymentActionsChildrenProps) => any;
}

export interface IWithDeploymentActionsState {
  showActionPromptDialog: boolean;
  targetId?: string;
  promptDialogButtonText?: string;
  promptDialogIcon?: ConfirmationIconType;
  promptDialogText?: string;
  promptDialogTitle?: string;
  handleAction?: () => void;
}

export class WithDeploymentActions extends React.Component<
  IWithDeploymentActionsProps,
  IWithDeploymentActionsState
> {
  constructor(props: IWithDeploymentActionsProps) {
    super(props);
    this.state = {
      showActionPromptDialog: false,
    };
    this.handleAction = this.handleAction.bind(this);
    this.handleActionCancel = this.handleActionCancel.bind(this);
    this.promptForAction = this.promptForAction.bind(this);
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

  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <UIContext.Consumer>
            {({ pushNotification }) => (
              <WithIntegrationHelpers>
                {({ deployIntegration, undeployIntegration, replaceDraft }) => {
                  const startDeploymentAction: IMenuActions = {
                    label: t('shared:Start'),
                    onClick: () =>
                      this.promptForAction({
                        handleAction: async () => {
                          pushNotification(
                            i18n.t('integrations:PublishingIntegrationMessage'),
                            'info'
                          );
                          try {
                            await deployIntegration(
                              this.props.integrationId,
                              this.props.deployment.version!,
                              true
                            );
                          } catch (err) {
                            pushNotification(
                              i18n.t(
                                'integrations:PublishingIntegrationFailedMessage',
                                {
                                  error: err.errorMessage || err.message || err,
                                }
                              ),
                              'warning'
                            );
                          }
                        },
                        promptDialogButtonStyle: ConfirmationButtonStyle.NORMAL,
                        promptDialogButtonText: t('shared:Start'),
                        promptDialogIcon: ConfirmationIconType.NONE,
                        promptDialogText: t(
                          'integrations:publishDeploymentModal',
                          {
                            name: this.props.integrationName,
                            version: this.props.deployment.version!,
                          }
                        ),
                        promptDialogTitle: t(
                          'integrations:publishDeploymentModalTitle'
                        ),
                      } as IPromptActionOptions),
                  };
                  const stopDeploymentAction: IMenuActions = {
                    label: t('shared:Stop'),
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
                              this.props.integrationId,
                              this.props.deployment.version!
                            );
                          } catch (err) {
                            pushNotification(
                              i18n.t(
                                'integrations:UnpublishingIntegrationFailedMessage',
                                {
                                  error: err.errorMessage || err.message || err,
                                }
                              ),
                              'warning'
                            );
                          }
                        },
                        promptDialogButtonStyle: ConfirmationButtonStyle.NORMAL,
                        promptDialogButtonText: t('shared:Stop'),
                        promptDialogIcon: ConfirmationIconType.NONE,
                        promptDialogText: t(
                          'integrations:unpublishIntegrationModal',
                          { name: this.props.integrationName }
                        ),
                        promptDialogTitle: t(
                          'integrations:unpublishIntegrationModalTitle'
                        ),
                      } as IPromptActionOptions),
                  };

                  const replaceDraftAction: IMenuActions = {
                    label: t('integrations:ReplaceDraft'),
                    onClick: () =>
                      this.promptForAction({
                        handleAction: async () => {
                          pushNotification(
                            i18n.t('integrations:ReplacedDraftMessage'),
                            'info'
                          );
                          try {
                            await replaceDraft(
                              this.props.integrationId,
                              this.props.deployment.version!
                            );
                          } catch (err) {
                            pushNotification(
                              i18n.t('integrations:ReplaceDraftFailedMessage', {
                                error: err.errorMessage || err.message || err,
                              }),
                              'warning'
                            );
                          }
                        },
                        promptDialogButtonStyle: ConfirmationButtonStyle.NORMAL,
                        promptDialogButtonText: t('shared:ReplaceDraft'),
                        promptDialogIcon: ConfirmationIconType.NONE,
                        promptDialogText: t(
                          'integrations:ReplaceDraftModalMessage',
                          { name: this.props.integrationName }
                        ),
                        promptDialogTitle: t(
                          'integrations:ReplaceDraftModalTitle'
                        ),
                      } as IPromptActionOptions),
                  };

                  return (
                    <>
                      {this.state.showActionPromptDialog && (
                        <ConfirmationDialog
                          buttonStyle={ConfirmationButtonStyle.NORMAL}
                          i18nCancelButtonText={t('shared:Cancel')}
                          i18nConfirmButtonText={
                            this.state.promptDialogButtonText!
                          }
                          i18nConfirmationMessage={this.state.promptDialogText!}
                          i18nTitle={this.state.promptDialogTitle!}
                          icon={this.state.promptDialogIcon!}
                          showDialog={this.state.showActionPromptDialog}
                          onCancel={this.handleActionCancel}
                          onConfirm={this.handleAction}
                        />
                      )}
                      {this.props.children({
                        replaceDraftAction,
                        startDeploymentAction,
                        stopDeploymentAction,
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
  }
}
