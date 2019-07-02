import * as H from '@syndesis/history';
import {
  //  Button,
  DropdownKebab,
  Icon,
  ListView,
  ListViewIcon,
  ListViewInfoItem,
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
  ProgressWithLink,
} from '../../Shared';
import {
  BUILDING,
  CONFIGURING,
  DEPLOYING,
  RUNNING,
  VirtualizationPublishState,
} from './models';
import { VirtualizationPublishStatus } from './VirtualizationPublishStatus';

import './VirtualizationListItem.css';

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
  i18nUnpublishInProgress: string;
  i18nUnpublishModalMessage: string;
  i18nUnpublishModalTitle: string;
  icon?: string;
  odataUrl?: string;
  onDelete: (virtualizationName: string) => void;
  /* TD-636: Commented out for TP 
  onExport: (virtualizationName: string) => void; */
  onPublish: (virtualizationName: string, hasViews: boolean) => void;
  onUnpublish: (virtualizationName: string) => void;
  publishingCurrentStep?: number;
  publishingLogUrl?: string;
  publishingTotalSteps?: number;
  publishingStepText?: string;
  serviceVdbName: string;
  virtualizationName: string;
  virtualizationViewNames: string[];
  virtualizationDescription: string;
}

export const VirtualizationListItem: React.FunctionComponent<
  IVirtualizationListItemProps
> = props => {
  const [showConfirmationDialog, setShowConfirmationDialog] = React.useState(false);

  const doCancel = () => {
    setShowConfirmationDialog(false);
  };

  const getEditTooltip = (): JSX.Element => {
    return (
      <Tooltip id="detailsTip">
        {props.i18nEditTip ? props.i18nEditTip : props.i18nEdit}
      </Tooltip>
    );
  }

  const doDelete = () => {
    setShowConfirmationDialog(false);

    // TODO: disable components while delete is processing
    props.onDelete(props.virtualizationName);
  };

  const doPublish = () => {
    if (props.virtualizationName) {
      props.onPublish(
        props.virtualizationName,
        props.virtualizationViewNames.length > 0
      );
    }
  }

  const doUnpublish = () => {
    setShowConfirmationDialog(false);

    if (props.serviceVdbName) {
      props.onUnpublish(props.serviceVdbName);
    }
  }

  const showConfirmDialog = () => {
    setShowConfirmationDialog(true);
  }

  // Determine published state
  const isPublished =
    props.currentPublishedState === RUNNING ? true : false;

  const publishInProgress =
    props.currentPublishedState === BUILDING ||
      props.currentPublishedState === CONFIGURING ||
      props.currentPublishedState === DEPLOYING
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
      <ListViewItem
        data-testid={`virtualization-list-item-${toValidHtmlId(
          props.virtualizationName
        )}-list-item`}
        actions={
          <div className="form-group">
            {publishInProgress ? (
              <div
                data-testid={'virtualization-list-item-progress'}
                className={'virtualization-list-item-progress'}
              >
                <ProgressWithLink
                  logUrl={props.publishingLogUrl}
                  value={
                    props.publishingStepText
                      ? props.publishingStepText
                      : ''
                  }
                  currentStep={
                    props.publishingCurrentStep
                      ? props.publishingCurrentStep
                      : 0
                  }
                  totalSteps={
                    props.publishingTotalSteps
                      ? props.publishingTotalSteps
                      : 4
                  }
                  i18nLogUrlText={props.i18nPublishLogUrlText}
                />
              </div>
            ) : (
                <VirtualizationPublishStatus
                  currentState={props.currentPublishedState}
                  i18nPublished={props.i18nPublished}
                  i18nUnpublished={props.i18nDraft}
                  i18nPublishInProgress={props.i18nPublishInProgress}
                  i18nUnpublishInProgress={props.i18nUnpublishInProgress}
                  i18nError={props.i18nError}
                />
              )}
            <OverlayTrigger overlay={getEditTooltip()} placement="top">
              <ButtonLink
                data-testid={'virtualization-list-item-edit-button'}
                href={props.detailsPageLink}
                as={'primary'}
              >
                {props.i18nEdit}
              </ButtonLink>
            </OverlayTrigger>
            <DropdownKebab
              id={`virtualization-${
                props.virtualizationName
                }-action-menu`}
              pullRight={true}
            >
              <MenuItem onClick={showConfirmDialog}>
                {props.i18nDelete}
              </MenuItem>
              {/* TD-636: Commented out for TP 
                <MenuItem onClick={handleExport}>
                  {props.i18nExport}
                </MenuItem> */}
              <MenuItem
                onClick={
                  isPublished || publishInProgress
                    ? doUnpublish
                    : doPublish
                }
              >
                {isPublished || publishInProgress
                  ? props.i18nUnpublish
                  : props.i18nPublish}
              </MenuItem>
            </DropdownKebab>
          </div>
        }
        heading={props.virtualizationName}
        description={
          props.virtualizationDescription
            ? props.virtualizationDescription
            : ''
        }
        additionalInfo={[
          <ListViewInfoItem key={1}>
            {props.odataUrl && (
              <span>
                <a
                  data-testid={'virtualization-list-item-odataUrl'}
                  target="_blank"
                  href={props.odataUrl}
                >
                  {props.odataUrl}
                  <Icon
                    className={'virtualization-list-item-odata-link-icon'}
                    name={'external-link'}
                  />
                </a>
              </span>
            )}
          </ListViewInfoItem>,
        ]}
        hideCloseIcon={true}
        leftContent={
          props.icon ? (
            <div className="blank-slate-pf-icon">
              <img
                src={props.icon}
                alt={props.virtualizationName}
                width={46}
              />
            </div>
          ) : (
              <ListViewIcon name={'cube'} />
            )
        }
        stacked={true}
      >
        {props.children ? (
          <ListView>{props.children}</ListView>
        ) : null}
      </ListViewItem>
    </>
  );
}
