import * as H from '@syndesis/history';
import { usePrevious } from '@syndesis/utils';
import {
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
} from '../../Shared';
import {
  DELETE_SUBMITTED,
  FAILED,
  NOTFOUND,
  RUNNING,
  SUBMITTED,
  VirtualizationPublishState,
} from './models';
import { PublishStatusWithProgress } from './PublishStatusWithProgress';
import './VirtualizationListItem.css';

export interface IVirtualizationListItemProps {
  /**
   * `true` if a publish step is in progress
   */
  isProgressWithLink: boolean;
  i18nDeleteInProgressText: string;
  i18nPublishInProgressText: string;
  i18nUnpublishInProgressText: string;
  i18nPublishState: string;
  /**
   * The type of label that shows to the left of the `Edit` button.
   */
  labelType: 'danger' | 'primary' | 'default';
  /**
   * The publish state returned from the backend.
   */
  currentPublishedState: VirtualizationPublishState;
  detailsPageLink: H.LocationDescriptor;
  hasViews: boolean;
  i18nCancelText: string;
  i18nDelete: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nEdit: string;
  i18nEditTip?: string;
  i18nExport: string;
  i18nInUseText: string;
  i18nPublish: string;
  i18nPublishLogUrlText: string;
  i18nUnpublish: string;
  i18nUnpublishModalMessage: string;
  i18nUnpublishModalTitle: string;
  icon?: string;
  odataUrl?: string;

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
  publishingCurrentStep?: number;
  publishingLogUrl?: string;
  publishingTotalSteps?: number;
  publishingStepText?: string;
  usedBy: string[];
  virtualizationName: string;
  virtualizationDescription: string;
}

export const VirtualizationListItem: React.FunctionComponent<
  IVirtualizationListItemProps
> = props => {
  const [showConfirmationDialog, setShowConfirmationDialog] = React.useState(
    false
  );
  const [labelType, setLabelType] = React.useState(props.labelType);
  const [publishStateText, setPublishStateText] = React.useState(props.i18nPublishState);
  const prevPublishedState = usePrevious(props.currentPublishedState);
  const [working, setWorking] = React.useState(false);

  /**
   * Side effects of setting `props.i18nPublishState`.
   */
  React.useEffect(() => {
    let changeText = true;

    if (props.i18nPublishState) {
      if (
        (props.currentPublishedState === RUNNING && prevPublishedState === DELETE_SUBMITTED)
        || (props.currentPublishedState === NOTFOUND && prevPublishedState === SUBMITTED)
      ) {
        changeText = false;
      } else if (
        props.currentPublishedState !== NOTFOUND
        && props.currentPublishedState !== RUNNING
        && props.currentPublishedState !== FAILED
      ) {
        changeText = false;
      }
    }

    if (changeText) {
      setPublishStateText(props.i18nPublishState);
    }

    // check to see if no longer in-progress
    if (
      props.currentPublishedState === NOTFOUND
      || props.currentPublishedState === RUNNING
      || props.currentPublishedState === FAILED
    ) {
      setWorking(false);
    }
  }, [props.i18nPublishState]);

  React.useEffect(() => {
    setLabelType(props.labelType);
  }, [props.labelType]);

  const doCancel = () => {
    setShowConfirmationDialog(false);
  };

  const getEditTooltip = (): JSX.Element => {
    return (
      <Tooltip id="detailsTip">
        {props.i18nEditTip ? props.i18nEditTip : props.i18nEdit}
      </Tooltip>
    );
  };

  // Here are the publish state transitions returned from server for when an unpublish is initiated:
  //   DELETE_SUBMITTED > RUNNING > DELETE_SUBMITTED > NOTFOUND
  // The transitiong from DELETE_SUBMITTED to RUNNING seems to be a bug.
  //
  // Here are the publish state transitions returned from server for when a publish is initiated:
  //   SUBMITTED > NOTFOUND > SUBMITTED > CONFIGURING > BUILDING > DEPLOYING > RUNNING
  // The transitiong from SUBMITTED to NOTFOUND seems to be a bug.
  //
  // The following code is a workaround.
  const applyInProgressWorkaround = () => {
    let isInProgress = false;

    if (
      (props.currentPublishedState === RUNNING &&
        prevPublishedState === DELETE_SUBMITTED) ||
      (props.currentPublishedState === NOTFOUND &&
        prevPublishedState === SUBMITTED)
    ) {
      isInProgress = true;
    } else if (
      props.currentPublishedState !== NOTFOUND &&
      props.currentPublishedState !== RUNNING &&
      props.currentPublishedState !== FAILED
    ) {
      isInProgress = true;
    }

    if (working !== isInProgress) {
      setWorking(isInProgress);
    }
  }

  const doDelete = async () => {
    setWorking(true);
    const saveText = publishStateText;
    const saveLabelType = labelType;
    setLabelType('default');
    setPublishStateText(props.i18nDeleteInProgressText);
    setShowConfirmationDialog(false);
    const result = await props.onDelete(props.virtualizationName);

    // restore previous settings when delete fails
    if (result === 'FAILED') {
      setWorking(false);
      setPublishStateText(saveText);
      setLabelType(saveLabelType);
    }

    applyInProgressWorkaround();
  };

  const doExport = () => {
    props.onExport(props.virtualizationName);
  }

  const doPublish = async () => {
    setWorking(true);
    const saveText = publishStateText;
    const saveLabelType = labelType;
    setLabelType('default');
    setPublishStateText(props.i18nPublishInProgressText);
    const result = await props.onPublish(
      props.virtualizationName,
      props.hasViews
    );

    // restore previous settings when publish fails
    if (result === 'FAILED') {
      setWorking(false);
      setPublishStateText(saveText);
      setLabelType(saveLabelType);
    }

    applyInProgressWorkaround();
  };

  const doUnpublish = async () => {
    setWorking(true);
    const saveText = publishStateText;
    const saveLabelType = labelType;
    setLabelType('default');
    setPublishStateText(props.i18nUnpublishInProgressText);
    setShowConfirmationDialog(false);
    const result = await props.onUnpublish(props.virtualizationName);

    // restore previous settings when unpublish fails
    if (result === 'FAILED') {
      setWorking(false);
      setPublishStateText(saveText);
      setLabelType(saveLabelType);
    }

    applyInProgressWorkaround();
  };

  const showConfirmDialog = () => {
    if (props.usedBy.length < 1) {
      setShowConfirmationDialog(true);
    }
  };

  // Determine published state
  const isPublished =
    props.currentPublishedState === RUNNING &&
    prevPublishedState !== DELETE_SUBMITTED;

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
            <PublishStatusWithProgress
              isProgressWithLink={props.isProgressWithLink}
              i18nPublishState={publishStateText}
              labelType={labelType}
              i18nPublishLogUrlText={props.i18nPublishLogUrlText}
              publishingCurrentStep={props.publishingCurrentStep}
              publishingLogUrl={props.publishingLogUrl}
              publishingTotalSteps={props.publishingTotalSteps}
              publishingStepText={props.publishingStepText}
            />
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
              id={`virtualization-${props.virtualizationName}-action-menu`}
              pullRight={true}
              data-testid={`virtualization-list-item-${
                props.virtualizationName
              }-dropdown-kebab`}
            >
              <MenuItem
                className={'virtualization-list-item__menuItem'}
                onClick={showConfirmDialog}
                disabled={
                  props.usedBy.length > 0 ||
                  working ||
                  props.isProgressWithLink ||
                  isPublished
                }
                data-testid={`virtualization-list-item-${
                  props.virtualizationName
                }-delete`}
              >
                {props.i18nDelete}
              </MenuItem>
              {/* TD-636: Commented out for TP
                <MenuItem onClick={handleExport}>
                  {props.i18nExport}
                </MenuItem> */}
              <MenuItem
                className={'virtualization-list-item__menuItem'}
                onClick={isPublished ? doUnpublish : doPublish}
                disabled={
                  props.usedBy.length > 0 ||
                  working ||
                  props.isProgressWithLink ||
                  !props.virtualizationName
                }
              >
                {isPublished ? props.i18nUnpublish : props.i18nPublish}
              </MenuItem>
              <MenuItem
                data-testid={`virtualization-list-item-${
                  props.virtualizationName
                }-export`}
                onClick={doExport}
              >
                {props.i18nExport}
              </MenuItem>
            </DropdownKebab>
          </div>
        }
        heading={props.virtualizationName}
        description={
          props.virtualizationDescription ? props.virtualizationDescription : ''
        }
        additionalInfo={[
          <ListViewInfoItem key={1} stacked={true}>
            {props.i18nInUseText}
          </ListViewInfoItem>,
          <ListViewInfoItem key={2} stacked={true}>
            {props.odataUrl && (
              <span className={'virtualization-list-item__odata-span'}>
                <a
                  className={'virtualization-list-item__odata-anchor'}
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
              <img src={props.icon} alt={props.virtualizationName} width={46} />
            </div>
          ) : (
            <ListViewIcon name={'cube'} />
          )
        }
        stacked={true}
      >
        {props.children ? <ListView>{props.children}</ListView> : null}
      </ListViewItem>
    </>
  );
};
