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
  FAILED,
  NOTFOUND,
  RUNNING,
  VirtualizationPublishState,
} from '../models';

import './ViewHeaderBreadcrumb.css';

export interface IViewHeaderBreadcrumbProps {
  isSubmitted: boolean; // `true` if publish/unpublish have been initiated but publish state has not reflected this
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
  i18nUnpublish: string;
  i18nUnpublishModalMessage: string;
  i18nUnpublishModalTitle: string;

  /**
   * Callback that will delete the virtualization.
   */
  onDelete: () => Promise<void>;

  /**
   * Callback that will export the virtualization.
   */
  onExport: () => void;

  /**
   * Callback that will publish the virtualization.
   */
  onPublish: () => Promise<void>;

  /**
   * Callback that will unpublish the virtualization.
   */
  onUnpublish: () => Promise<void>;
  virtualizationName: string;
  usedInIntegration: boolean;
}

export const ViewHeaderBreadcrumb: React.FunctionComponent<
  IViewHeaderBreadcrumbProps
> = props => {
  const [showConfirmationDialog, setShowConfirmationDialog] = React.useState(
    false
  );
  const [inProgress, setInProgress] = React.useState(false);
  const [isPublished, setIsPublished] = React.useState(false);
  const [buttonText, setButtonText] = React.useState(props.i18nPublish);

  React.useEffect(() => {
    let working = false;
    if (
      props.currentPublishedState !== NOTFOUND &&
      props.currentPublishedState !== RUNNING &&
      props.currentPublishedState !== FAILED
    ) {
      working = true;
    }
    setInProgress(working);

    // update button text
    if (!working) {
      const published = props.currentPublishedState === RUNNING;
      setIsPublished(published);
      if (published) {
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
    await props.onDelete().catch(() => {
      // restore button text
      setButtonText(props.i18nPublish);
      setInProgress(false);
    });
  };

  const doExport = () => {
    props.onExport();
  }

  const doPublish = async () => {
    setInProgress(true);
    await props.onPublish().catch(() => {
      // restore button text
      setButtonText(props.i18nPublish);
      setInProgress(false);
    });
  };

  const doUnpublish = async () => {
    setShowConfirmationDialog(false);
    setInProgress(true);
    await props.onUnpublish().catch(() => {
      // restore button text
      setButtonText(props.i18nUnpublish);
      setInProgress(false);
    });
  };

  const showConfirmDialog = () => {
    if (!props.usedInIntegration) {
      setShowConfirmationDialog(true);
    }
  };

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
              disabled={
                props.usedInIntegration || inProgress || props.isSubmitted
              }
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
                disabled={
                  props.usedInIntegration ||
                  inProgress ||
                  isPublished ||
                  props.isSubmitted
                }
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
