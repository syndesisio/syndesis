import * as H from '@syndesis/history';
import {
  //  Button,
  DropdownKebab,
  ListView,
  ListViewIcon,
  ListViewItem,
  MenuItem,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink } from '../../Layout';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
} from '../../Shared';
import {
  BUILDING,
  CONFIGURING,
  DEPLOYING,
  RUNNING,
  SUBMITTED,
  VirtualizationPublishState,
} from './models';
import { VirtualizationPublishStatus } from './VirtualizationPublishStatus';
import { VirtualizationPublishStatusDetail } from './VirtualizationPublishStatusDetail';

export interface IVirtualizationListItemProps {
  currentPublishedState: VirtualizationPublishState;
  detailsPageLink: H.LocationDescriptor;
  i18nCancelText: string;
  i18nDelete: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nDraft: string;
  i18nError: string;
  i18nEdit: string;
  i18nEditTip?: string;
  /* TD-636: Commented out for TP 
  i18nExport: string; */
  i18nPublish: string;
  i18nPublished: string;
  i18nPublishLogUrlText: string;
  i18nPublishInProgress: string;
  i18nUnpublish: string;
  i18nUnpublishModalMessage: string;
  i18nUnpublishModalTitle: string;
  icon?: string;
  onDelete: (virtualizationName: string) => void;
  /* TD-636: Commented out for TP 
  onExport: (virtualizationName: string) => void; */
  onPublish: (virtualizationName: string) => void;
  onUnpublish: (virtualizationName: string) => void;
  publishingCurrentStep?: number;
  publishingLogUrl?: string;
  publishingTotalSteps?: number;
  publishingStepText?: string;
  serviceVdbName: string;
  virtualizationName: string;
  virtualizationDescription: string;
}

export interface IVirtualizationListItemState {
  showConfirmationDialog: boolean;
}

export class VirtualizationListItem extends React.Component<
  IVirtualizationListItemProps,
  IVirtualizationListItemState
> {
  public constructor(props: IVirtualizationListItemProps) {
    super(props);
    this.state = {
      showConfirmationDialog: false, // initial visibility of confirmation dialog
    };
    this.handleCancel = this.handleCancel.bind(this);
    this.handleDelete = this.handleDelete.bind(this);
    /* TD-636: Commented out for TP
    this.handleExport = this.handleExport.bind(this); */
    this.handleUnpublish = this.handleUnpublish.bind(this);
    this.handlePublish = this.handlePublish.bind(this);
    this.showConfirmationDialog = this.showConfirmationDialog.bind(this);
  }

  public getEditTooltip() {
    return (
      <Tooltip id="detailsTip">
        {this.props.i18nEditTip ? this.props.i18nEditTip : this.props.i18nEdit}
      </Tooltip>
    );
  }

  public handleCancel() {
    this.setState({
      showConfirmationDialog: false, // hide dialog
    });
  }

  public handleDelete() {
    this.setState({
      showConfirmationDialog: false, // hide dialog
    });

    // TODO: disable components while delete is processing
    if (this.props.virtualizationName) {
      this.props.onDelete(this.props.virtualizationName);
    }
  }

  /* TD-636: Commented out for TP
  public handleExport() {
    if (this.props.virtualizationName) {
      this.props.onExport(this.props.virtualizationName);
    }
  } */

  public handlePublish() {
    if (this.props.virtualizationName) {
      this.props.onPublish(this.props.virtualizationName);
    }
  }

  public handleUnpublish() {
    this.setState({
      showConfirmationDialog: false, // hide dialog
    });

    if (this.props.serviceVdbName) {
      this.props.onUnpublish(this.props.serviceVdbName);
    }
  }

  public showConfirmationDialog() {
    this.setState({
      showConfirmationDialog: true,
    });
  }

  public handleAcceptConfirmation() {
    const isPublished =
      this.props.currentPublishedState === RUNNING ? true : false;
    if (isPublished) {
      this.handleUnpublish();
    } else {
      this.handleDelete();
    }
  }

  public render() {
    // Determine published state
    const isPublished =
      this.props.currentPublishedState === RUNNING ? true : false;
    const publishInProgress =
      this.props.currentPublishedState === BUILDING ||
      this.props.currentPublishedState === CONFIGURING ||
      this.props.currentPublishedState === DEPLOYING ||
      this.props.currentPublishedState === SUBMITTED
        ? true
        : false;

    return (
      <>
        <ConfirmationDialog
          buttonStyle={
            isPublished
              ? ConfirmationButtonStyle.WARNING
              : ConfirmationButtonStyle.DANGER
          }
          i18nCancelButtonText={this.props.i18nCancelText}
          i18nConfirmButtonText={
            isPublished ? this.props.i18nUnpublish : this.props.i18nDelete
          }
          i18nConfirmationMessage={
            isPublished
              ? this.props.i18nUnpublishModalMessage
              : this.props.i18nDeleteModalMessage
          }
          i18nTitle={
            isPublished
              ? this.props.i18nUnpublishModalTitle
              : this.props.i18nDeleteModalTitle
          }
          icon={
            isPublished
              ? ConfirmationIconType.WARNING
              : ConfirmationIconType.DANGER
          }
          showDialog={this.state.showConfirmationDialog}
          onCancel={this.handleCancel}
          onConfirm={this.handleDelete}
        />
        <ListViewItem
          data-testid={`virtualization-list-item-${toValidHtmlId(
            this.props.virtualizationName
          )}-list-item`}
          actions={
            <div className="form-group">
              {publishInProgress ? (
                <VirtualizationPublishStatusDetail
                  logUrl={this.props.publishingLogUrl}
                  stepText={this.props.publishingStepText}
                  currentStep={this.props.publishingCurrentStep}
                  totalSteps={this.props.publishingTotalSteps}
                  i18nPublishInProgress={this.props.i18nPublishInProgress}
                  i18nLogUrlText={this.props.i18nPublishLogUrlText}
                />
              ) : (
                <VirtualizationPublishStatus
                  currentState={this.props.currentPublishedState}
                  i18nPublished={this.props.i18nPublished}
                  i18nUnpublished={this.props.i18nDraft}
                  i18nError={this.props.i18nError}
                />
              )}
              <OverlayTrigger overlay={this.getEditTooltip()} placement="top">
                <ButtonLink
                  data-testid={'virtualization-list-item-edit-button'}
                  href={this.props.detailsPageLink}
                  as={'primary'}
                >
                  {this.props.i18nEdit}
                </ButtonLink>
              </OverlayTrigger>
              <DropdownKebab
                id={`virtualization-${
                  this.props.virtualizationName
                }-action-menu`}
                pullRight={true}
              >
                <MenuItem onClick={this.showConfirmationDialog}>
                  {this.props.i18nDelete}
                </MenuItem>
                {/* TD-636: Commented out for TP 
                <MenuItem onClick={this.handleExport}>
                  {this.props.i18nExport}
                </MenuItem> */}
                <MenuItem
                  onClick={
                    isPublished || publishInProgress
                      ? this.handleUnpublish
                      : this.handlePublish
                  }
                >
                  {isPublished || publishInProgress
                    ? this.props.i18nUnpublish
                    : this.props.i18nPublish}
                </MenuItem>
              </DropdownKebab>
            </div>
          }
          heading={this.props.virtualizationName}
          description={
            this.props.virtualizationDescription
              ? this.props.virtualizationDescription
              : ''
          }
          hideCloseIcon={true}
          leftContent={
            this.props.icon ? (
              <div className="blank-slate-pf-icon">
                <img
                  src={this.props.icon}
                  alt={this.props.virtualizationName}
                  width={46}
                />
              </div>
            ) : (
              <ListViewIcon name={'cube'} />
            )
          }
          stacked={true}
        >
          {this.props.children ? (
            <ListView>{this.props.children}</ListView>
          ) : null}
        </ListViewItem>
      </>
    );
  }
}
