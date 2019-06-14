import * as H from '@syndesis/history';
import { DropdownKebab, MenuItem } from 'patternfly-react';
import React from 'react';
import { Link } from 'react-router-dom';

import { Breadcrumb, ButtonLink } from '../../../Layout';

import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
} from '../../../Shared';

import {
  BUILDING,
  CONFIGURING,
  DEPLOYING,
  RUNNING,
  SUBMITTED,
  VirtualizationPublishState,
} from '../models';

export interface IViewHeaderBreadcrumbProps {
  currentPublishedState: VirtualizationPublishState;
  dashboardHref: H.LocationDescriptor;
  dataHref: H.LocationDescriptor;
  dashboardString: string;
  dataString: string;
  i18nViews: string;
  i18nCancelText: string;
  i18nDelete: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  /* TD-636: Commented out for TP
  i18nExport: string; */
  i18nPublish: string;
  i18nUnpublish: string;
  i18nUnpublishModalMessage: string;
  i18nUnpublishModalTitle: string;
  onDelete: (virtualizationName: string) => void;
  /* TD-636: Commented out for TP
  onExport: (virtualizationName: string) => void; */
  onPublish: (virtualizationName: string, hasViews: boolean) => void;
  onUnpublish: (virtualizationName: string) => void;
  serviceVdbName: string;
  virtualizationName: string;
  hasViews: boolean;
}

export interface IViewHeaderBreadcrumbState {
  showConfirmationDialog: boolean;
}

export class ViewHeaderBreadcrumb extends React.Component<
  IViewHeaderBreadcrumbProps,
  IViewHeaderBreadcrumbState
> {
  public constructor(props: IViewHeaderBreadcrumbProps) {
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

  public handlePublish() {
    if (this.props.virtualizationName) {
      this.props.onPublish(this.props.virtualizationName, this.props.hasViews);
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
          onConfirm={isPublished ? this.handleUnpublish : this.handleDelete}
        />
        <Breadcrumb
          actions={
            <>
              <ButtonLink
                data-testid={'virtualization-detail-breadcrumb-publish-button'}
                className="btn btn-primary"
                onClick={
                  isPublished || publishInProgress
                    ? this.handleUnpublish
                    : this.handlePublish
                }
              >
                {isPublished || publishInProgress
                  ? this.props.i18nUnpublish
                  : this.props.i18nPublish}
              </ButtonLink>
              <DropdownKebab
                id={`virtualization-${
                  this.props.virtualizationName
                }-action-menu`}
                pullRight={true}
              >
                <MenuItem onClick={this.showConfirmationDialog}>
                  {this.props.i18nDelete}
                </MenuItem>
              </DropdownKebab>
            </>
          }
        >
          <Link
            data-testid={'virtualization-views-page-home-link'}
            to={this.props.dashboardHref}
          >
            {this.props.dashboardString}
          </Link>
          <Link
            data-testid={'virtualization-views-page-virtualizations-link'}
            to={this.props.dataHref}
          >
            {this.props.dataString}
          </Link>
          <span>
            {this.props.virtualizationName + ' '}
            {this.props.i18nViews}
          </span>
        </Breadcrumb>
      </>
    );
  }
}
