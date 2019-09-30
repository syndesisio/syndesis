import * as H from '@syndesis/history';
import { Button, DropdownKebab } from 'patternfly-react';
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
  i18nResolving: string;
  i18nUnpublish: string;
  i18nUnpublishModalMessage: string;
  i18nUnpublishModalTitle: string;
  onDelete: (virtualizationName: string) => Promise<boolean>;
  onExport: (virtualizationName: string) => void;
  onPublish: (virtualizationName: string, hasViews: boolean) => void;
  onUnpublish: (virtualizationName: string) => void;
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
  const [buttonText, setButtonText] = React.useState(props.i18nResolving);

  React.useEffect(() => {
    const updated =
      props.currentPublishedState !== NOTFOUND &&
      props.currentPublishedState !== RUNNING &&
      props.currentPublishedState !== FAILED;

    if (inProgress !== updated) {
      setInProgress(updated);
    }
  }, [props.currentPublishedState]);

  React.useEffect(() => {
    if (props.currentPublishedState === RUNNING) {
      setButtonText(props.i18nUnpublish);
    } else if (props.currentPublishedState === NOTFOUND) {
      setButtonText(props.i18nPublish);
    } else if (!props.currentPublishedState) {
      setButtonText(props.i18nResolving);
    }
  }, [props.currentPublishedState]);

  const doCancel = () => {
    setShowConfirmationDialog(false);
  };

  const doDelete = () => {
    setShowConfirmationDialog(false);

    // TODO: disable components while delete is processing
    if (props.virtualizationName) {
      setInProgress(true);
      const save = buttonText;

      // restore button text if delete fails
      if (!props.onDelete(props.virtualizationName)) {
        setButtonText(save);
      }
    }
  };

  const doExport = () => {
    props.onExport(props.virtualizationName);
  }

  const doPublish = () => {
    if (props.virtualizationName) {
      setInProgress(true);
      props.onPublish(props.virtualizationName, props.hasViews);
    }
  };

  const doUnpublish = () => {
    setShowConfirmationDialog(false);

    if (props.virtualizationName) {
      setInProgress(true);
      props.onUnpublish(props.virtualizationName);
    }
  };

  const showConfirmDialog = () => {
    if (!props.usedInIntegration) {
      setShowConfirmationDialog(true);
    }
  };

  const isPublished = () => {
    return props.currentPublishedState === RUNNING;
  };

  return (
    <>
      <ConfirmationDialog
        buttonStyle={
          isPublished()
            ? ConfirmationButtonStyle.WARNING
            : ConfirmationButtonStyle.DANGER
        }
        i18nCancelButtonText={props.i18nCancelText}
        i18nConfirmButtonText={
          isPublished() ? props.i18nUnpublish : props.i18nDelete
        }
        i18nConfirmationMessage={
          isPublished()
            ? props.i18nUnpublishModalMessage
            : props.i18nDeleteModalMessage
        }
        i18nTitle={
          isPublished()
            ? props.i18nUnpublishModalTitle
            : props.i18nDeleteModalTitle
        }
        icon={
          isPublished()
            ? ConfirmationIconType.WARNING
            : ConfirmationIconType.DANGER
        }
        showDialog={showConfirmationDialog}
        onCancel={doCancel}
        onConfirm={isPublished() ? doUnpublish : doDelete}
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
              onClick={isPublished() ? doUnpublish : doPublish}
              disabled={props.usedInIntegration || inProgress}
            >
              {buttonText}
            </ButtonLink>
            <DropdownKebab
              id={`virtualization-${props.virtualizationName}-action-menu`}
              pullRight={true}
            >
              <Button
                bsStyle={'default'}
                className={'view-header-breadcrumb__menuItem'}
                data-testid={'view-header-breadcrumb-delete-button'}
                disabled={props.usedInIntegration || inProgress}
                onClick={showConfirmDialog}
              >
                {props.i18nDelete}
              </Button>
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
