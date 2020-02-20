import { Tooltip } from '@patternfly/react-core';
import { ExternalLinkAltIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
import {
  DropdownKebab,
  ListView,
  ListViewIcon,
  ListViewInfoItem,
  ListViewItem,
  MenuItem,
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
  FAILED,
  NOTFOUND,
  RUNNING,
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
  i18nStopInProgressText: string;
  /**
   * The text to use for the label.
   */
  i18nPublishState: string;
  /**
   * The type of label that shows to the left of the `Edit` button.
   */
  labelType: 'danger' | 'primary' | 'default';
  /**
   * The publish state and version returned from the backend.
   */
  currentPublishedState: VirtualizationPublishState;
  currentPublishedVersion?: number;
  detailsPageLink: H.LocationDescriptor;
  hasViews: boolean;
  i18nCancelText: string;
  i18nDelete: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nEdit: string;
  i18nEditTip?: string;
  i18nInUseText: string;
  i18nPublish: string;
  i18nPublishLogUrlText: string;
  i18nViewODataUrlText: string;
  i18nStop: string;
  i18nStopModalMessage: string;
  i18nStopModalTitle: string;
  icon?: string;
  modified: boolean;
  odataUrl?: string;

  /**
   * @param virtualizationName the name of the virtualization being deleted
   */
  onDelete: (virtualizationName: string) => Promise<void>;

  /**
   * @param virtualizationName the name of the virtualization being published
   */
  onPublish: (virtualizationName: string, hasViews: boolean) => Promise<void>;

  /**
   * @param virtualizationName the name of the virtualization being stopped (unpublished)
   */
  onStop: (virtualizationName: string) => Promise<void>;
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
  const [publishStateText, setPublishStateText] = React.useState(
    props.i18nPublishState
  );
  const [working, setWorking] = React.useState(false);

  React.useEffect(() => {
    let changeText = true;

    if (props.i18nPublishState) {
      if (
        props.currentPublishedState !== NOTFOUND &&
        props.currentPublishedState !== RUNNING &&
        props.currentPublishedState !== FAILED
      ) {
        changeText = false;
      }
    }

    if (changeText) {
      setPublishStateText(props.i18nPublishState);
    }

    setWorking(
      props.currentPublishedState !== NOTFOUND &&
        props.currentPublishedState !== RUNNING &&
        props.currentPublishedState !== FAILED
    );
  }, [props.i18nPublishState, props.currentPublishedState]);

  React.useEffect(() => {
    setLabelType(props.labelType);
  }, [props.labelType]);

  const doCancel = () => {
    setShowConfirmationDialog(false);
  };

  const doDelete = async () => {
    setWorking(true);
    const saveText = publishStateText;
    const saveLabelType = labelType;
    setLabelType('default');
    setPublishStateText(props.i18nDeleteInProgressText);
    setShowConfirmationDialog(false);
    await props.onDelete(props.virtualizationName).catch(() => {
      // restore previous values
      setPublishStateText(saveText);
      setLabelType(saveLabelType);
    });
    setWorking(false);
  };

  const doPublish = async () => {
    setWorking(true);
    const saveText = publishStateText;
    const saveLabelType = labelType;
    setLabelType('default');
    setPublishStateText(props.i18nPublishInProgressText);
    await props.onPublish(props.virtualizationName, props.hasViews).catch(() => {
        // restore previous values
        setPublishStateText(saveText);
        setLabelType(saveLabelType);
      });
    setWorking(false);
  };

  const doStop = async () => {
    setWorking(true);
    const saveText = publishStateText;
    const saveLabelType = labelType;
    setLabelType('default');
    setPublishStateText(props.i18nStopInProgressText);
    setShowConfirmationDialog(false);
    await props.onStop(props.virtualizationName).catch(() => {
      // restore previous values
      setPublishStateText(saveText);
      setLabelType(saveLabelType);
    });
    setWorking(false);
  };

  const showConfirmDialog = () => {
    if (props.usedBy.length < 1) {
      setShowConfirmationDialog(true);
    }
  };

  // Determine if virtualization is running
  const isRunning = props.currentPublishedState === RUNNING;

  const shouldDisablePublish =
    working ||
    props.isProgressWithLink ||
    !props.virtualizationName ||
    (!props.modified && isRunning);

  const shouldDisableStop =
    props.usedBy.length > 0 ||
    working ||
    !isRunning ||
    !props.virtualizationName;

  const shouldDisableDelete =
    props.usedBy.length > 0 || working || props.isProgressWithLink || isRunning;

  return (
    <>
      <ConfirmationDialog
        buttonStyle={
          isRunning
            ? ConfirmationButtonStyle.WARNING
            : ConfirmationButtonStyle.DANGER
        }
        i18nCancelButtonText={props.i18nCancelText}
        i18nConfirmButtonText={isRunning ? props.i18nStop : props.i18nDelete}
        i18nConfirmationMessage={
          isRunning ? props.i18nStopModalMessage : props.i18nDeleteModalMessage
        }
        i18nTitle={
          isRunning ? props.i18nStopModalTitle : props.i18nDeleteModalTitle
        }
        icon={
          isRunning ? ConfirmationIconType.WARNING : ConfirmationIconType.DANGER
        }
        showDialog={showConfirmationDialog}
        onCancel={doCancel}
        onConfirm={isRunning ? doStop : doDelete}
      />
      <ListViewItem
        data-testid={`virtualization-list-item-${toValidHtmlId(
          props.virtualizationName
        )}-list-item`}
        actions={
          <div className="form-group">
            <PublishStatusWithProgress
              isProgressWithLink={props.isProgressWithLink}
              inListView={true}
              i18nPublishState={publishStateText}
              i18nPublishLogUrlText={props.i18nPublishLogUrlText}
              labelType={labelType}
              modified={props.modified}
              publishVersion={props.currentPublishedVersion}
              publishingCurrentStep={props.publishingCurrentStep}
              publishingLogUrl={props.publishingLogUrl}
              publishingTotalSteps={props.publishingTotalSteps}
              publishingStepText={props.publishingStepText}
            />
            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'editTip'}>
                  {props.i18nEditTip ? props.i18nEditTip : props.i18nEdit}
                </div>
              }
            >
              <ButtonLink
                data-testid={'virtualization-list-item-edit-button'}
                href={props.detailsPageLink}
                as={'primary'}
              >
                {props.i18nEdit}
              </ButtonLink>
            </Tooltip>
            <DropdownKebab
              id={`virtualization-${props.virtualizationName}-action-menu`}
              pullRight={true}
              data-testid={`virtualization-list-item-${props.virtualizationName}-dropdown-kebab`}
            >
              <MenuItem
                className={'virtualization-list-item__menuItem'}
                onClick={doPublish}
                disabled={shouldDisablePublish}
                data-testid={`virtualization-list-item-${props.virtualizationName}-publish`}
              >
                {props.i18nPublish}
              </MenuItem>
              <MenuItem
                className={'virtualization-list-item__menuItem'}
                onClick={doStop}
                disabled={shouldDisableStop}
                data-testid={`virtualization-list-item-${props.virtualizationName}-stop`}
              >
                {props.i18nStop}
              </MenuItem>
              <MenuItem
                className={'virtualization-list-item__menuItem'}
                onClick={showConfirmDialog}
                disabled={shouldDisableDelete}
                data-testid={`virtualization-list-item-${props.virtualizationName}-delete`}
              >
                {props.i18nDelete}
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
            {props.usedBy.length > 0 ? (
              props.i18nInUseText
            ) : (
              <span className={'virtualization-list-item__usedby-text'} />
            )}
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
                  {props.i18nViewODataUrlText}
                  <ExternalLinkAltIcon
                    className={'virtualization-list-item-odata-link-icon'}
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
