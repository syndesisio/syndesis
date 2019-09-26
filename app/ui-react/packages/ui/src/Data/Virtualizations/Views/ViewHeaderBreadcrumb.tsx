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
  virtualizationName: string;
  hasViews: boolean;
  usedInIntegration: boolean;
}

export const ViewHeaderBreadcrumb: React.FunctionComponent<
  IViewHeaderBreadcrumbProps
> = props => {

  const [showConfirmationDialog, setShowConfirmationDialog] = React.useState(false);

  const doCancel = () => {
    setShowConfirmationDialog(false);
  }

  const doDelete = () => {
    setShowConfirmationDialog(false);

    // TODO: disable components while delete is processing
    props.onDelete(props.virtualizationName);
  }

  const doPublish = () => {
    if (props.virtualizationName) {
      props.onPublish(props.virtualizationName, props.hasViews);
    }
  }

  const doUnpublish = () => {
    setShowConfirmationDialog(false);

    if (props.virtualizationName) {
      props.onUnpublish(props.virtualizationName);
    }
  }

  const showConfirmDialog = () => {
    if (!props.usedInIntegration) {
      setShowConfirmationDialog(true);
    }
  }

  const isPublished =
    props.currentPublishedState === RUNNING ? true : false;

  const publishInProgress =
    props.currentPublishedState === BUILDING ||
      props.currentPublishedState === CONFIGURING ||
      props.currentPublishedState === DEPLOYING ||
      props.currentPublishedState === SUBMITTED
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
        i18nCancelButtonText={props.i18nCancelText}
        i18nConfirmButtonText={
          isPublished ? props.i18nUnpublish : props.i18nDelete
        }
        i18nConfirmationMessage={
          isPublished
            ? props.i18nUnpublishModalMessage
            : props.i18nDeleteModalMessage
        }
        i18nTitle={
          isPublished
            ? props.i18nUnpublishModalTitle
            : props.i18nDeleteModalTitle
        }
        icon={
          isPublished
            ? ConfirmationIconType.WARNING
            : ConfirmationIconType.DANGER
        }
        showDialog={showConfirmationDialog}
        onCancel={doCancel}
        onConfirm={isPublished ? doUnpublish : doDelete}
      />
      <Breadcrumb
        actions={
          <>
            <ButtonLink
              data-testid={'virtualization-detail-breadcrumb-publish-button'}
              className="btn btn-primary"
              onClick={
                isPublished || publishInProgress
                  ? doUnpublish 
                  : doPublish
              }
              disabled={props.usedInIntegration}
            >
              {isPublished || publishInProgress
                ? props.i18nUnpublish
                : props.i18nPublish}
            </ButtonLink>
            <DropdownKebab
              id={`virtualization-${
                props.virtualizationName
                }-action-menu`}
              pullRight={true}
            >
              <MenuItem onClick={showConfirmDialog} disabled={props.usedInIntegration}>
                {props.i18nDelete}
              </MenuItem>
            </DropdownKebab>
          </>
        }
      >
        <Link
          data-testid={'virtualization-views-page-home-link'}
          to={props.dashboardHref}
        >
          {props.dashboardString}
        </Link>
        <Link
          data-testid={'virtualization-views-page-virtualizations-link'}
          to={props.dataHref}
        >
          {props.dataString}
        </Link>
        <span>
          {props.virtualizationName}
        </span>
      </Breadcrumb>
    </>
  );
}
