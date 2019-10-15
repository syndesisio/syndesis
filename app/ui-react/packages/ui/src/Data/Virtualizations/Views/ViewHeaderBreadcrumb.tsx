import * as H from '@syndesis/history';
import { usePrevious } from '@syndesis/utils';
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
  DELETE_DONE,
  DELETE_REQUEUE,
  DELETE_SUBMITTED,
  DEPLOYING,
  FAILED,
  NOTFOUND,
  RUNNING,
  SUBMITTED,
  VirtualizationPublishState,
} from '../models';

import './ViewHeaderBreadcrumb.css';

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
  i18nExport: string;
  i18nPublish: string;
  i18nPublishInProgress: string;
  i18nUnpublish: string;
  i18nUnpublishInProgress: string;
  i18nUnpublishModalMessage: string;
  i18nUnpublishModalTitle: string;

  /**
   * @param virtualizationName the name of the virtualization being deleted
   * @returns 'FAILED' if the publish operation threw an error; otherwise 'DELETED'
   */
  onDelete: (virtualizationName: string) => Promise<string>;
  onExport: (virtualizationName: string) => void;

  /**
   * @param virtualizationName the name of the virtualization being published
   * @returns 'FAILED' if the publish operation threw an error; otherwise the Teidd status
   */
  onPublish: (virtualizationName: string, hasViews: boolean) => Promise<string>;

  /**
   * @param virtualizationName the name of the virtualization being unpublished
   * @returns 'FAILED' if the unpublish operation threw an error; otherwise the build status
   */
  onUnpublish: (virtualizationName: string) => Promise<string>;
  virtualizationName: string;
  hasViews: boolean;
  usedInIntegration: boolean;
}

export const ViewHeaderBreadcrumb: React.FunctionComponent<
  IViewHeaderBreadcrumbProps
> = props => {
  const [showConfirmationDialog, setShowConfirmationDialog] = React.useState(
    false
  );
  const [inProgress, setInProgress] = React.useState(false);
  const [buttonText, setButtonText] = React.useState();
  const prevPublishedState = usePrevious(props.currentPublishedState);

  // Effect to update `inProgress` state.
  React.useEffect(() => {
    let currentInProgress = false;

    // see effects notes below on why this first check is needed
    if (
      (props.currentPublishedState === RUNNING &&
        prevPublishedState === DELETE_SUBMITTED) ||
      (props.currentPublishedState === NOTFOUND &&
        prevPublishedState === SUBMITTED)
    ) {
      currentInProgress = true;
    } else if (
      props.currentPublishedState !== NOTFOUND &&
      props.currentPublishedState !== RUNNING &&
      props.currentPublishedState !== FAILED
    ) {
      currentInProgress = true;
    }

    if (inProgress !== currentInProgress) {
      setInProgress(currentInProgress);
    }
  }, [props.currentPublishedState]);

  // Effect to update `buttonText` state.
  React.useEffect(() => {
    if (props.currentPublishedState === RUNNING) {
      // Here are the publish state transitions returned from server for when an unpublish is initiated:
      // DELETE_SUBMITTED > RUNNING > DELETE_SUBMITTED > NOTFOUND
      // The transitiong from DELETE_SUBMITTED to RUNNING seems to be a bug. The following code is a
      // workaround.
      if (prevPublishedState !== DELETE_SUBMITTED) {
        setButtonText(props.i18nUnpublish);
      }
    } else if (props.currentPublishedState === NOTFOUND) {
      // Here are the publish state transitions returned from server for when a publish is initiated:
      // SUBMITTED > NOTFOUND > SUBMITTED > CONFIGURING > BUILDING > DEPLOYING > RUNNING
      // The transitiong from SUBMITTED to NOTFOUND seems to be a bug. The following code is a
      // workaround.
      if (prevPublishedState !== SUBMITTED) {
        setButtonText(props.i18nPublish);
      }
    } else if (
      props.currentPublishedState === SUBMITTED ||
      props.currentPublishedState === BUILDING ||
      props.currentPublishedState === CONFIGURING ||
      props.currentPublishedState === DEPLOYING
    ) {
      setButtonText(props.i18nPublishInProgress);
    } else if (
      props.currentPublishedState === DELETE_DONE ||
      props.currentPublishedState === DELETE_REQUEUE ||
      props.currentPublishedState === DELETE_SUBMITTED
    ) {
      setButtonText(props.i18nUnpublishInProgress);
    } else if (!props.currentPublishedState) {
      if (isPublished) {
        setButtonText(props.i18nUnpublish);
      } else {
        setButtonText(props.i18nPublish);
      }
    }
  }, [props.currentPublishedState]);

  const doCancel = () => {
    setShowConfirmationDialog(false);
  };

  const doDelete = async () => {
    setShowConfirmationDialog(false);
    setInProgress(true);
    const result = await props.onDelete(props.virtualizationName);
    if (result === 'FAILED') {
      setInProgress(false);
      setButtonText(props.i18nPublish);
    }
  };

  const doExport = () => {
    props.onExport(props.virtualizationName);
  }

  const doPublish = async () => {
    setInProgress(true);
    const result = await props.onPublish(
      props.virtualizationName,
      props.hasViews
    );

    // Check to see if unpublish failed. If it didn't fail, the publish state will be updated
    // by the properties and effects.
    if (result === 'FAILED') {
      setInProgress(false);
      setButtonText(props.i18nPublish);
    }
  };

  const doUnpublish = async () => {
    setShowConfirmationDialog(false);
    setInProgress(true);
    const result = await props.onUnpublish(props.virtualizationName);

    // Check to see if unpublish failed. If it didn't fail, the publish state will be updated
    // by the properties and effects.
    if (result === 'FAILED') {
      setInProgress(false);
      setButtonText(props.i18nUnpublish);
    }
  };

  const showConfirmDialog = () => {
    if (!props.usedInIntegration) {
      setShowConfirmationDialog(true);
    }
  };

  const isPublished = props.currentPublishedState === RUNNING;

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
              className="btn"
              data-testid={'virtualization-detail-breadcrumb-export-button'}
              onClick={doExport}
            >
              {props.i18nExport}
            </ButtonLink>
            &nbsp;&nbsp;
            <ButtonLink
              data-testid={'virtualization-detail-breadcrumb-publish-button'}
              className="btn btn-primary"
              onClick={isPublished ? doUnpublish : doPublish}
              disabled={props.usedInIntegration || inProgress}
            >
              {buttonText}
            </ButtonLink>
            <DropdownKebab
              id={`virtualization-${props.virtualizationName}-action-menu`}
              pullRight={true}
              data-testid={'view-header-breadcrumb-dropdown-kebab'}
            >
              <MenuItem
                className={'virtualization-list-item__menuItem'}
                onClick={showConfirmDialog}
                disabled={props.usedInIntegration || inProgress || isPublished}
                data-testid={'view-header-breadcrumb-delete-button'}
              >
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
        <span>{props.virtualizationName}</span>
      </Breadcrumb>
    </>
  );
};
