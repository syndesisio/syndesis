import {
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Popover,
  Split,
  SplitItem,
  Tooltip,
} from '@patternfly/react-core';
import { CubeIcon, ExternalLinkAltIcon, LockIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink } from '../../Layout';
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
  dropdownActions: JSX.Element;
  i18nEdit: string;
  i18nEditTip?: string;
  i18nInUseText: string;
  i18nPublishLogUrlText: string;
  i18nViewODataUrlText: string;
  icon?: string;
  modified: boolean;
  odataUrl?: string;
  publishingCurrentStep?: number;
  publishingLogUrl?: string;
  publishingTotalSteps?: number;
  publishingStepText?: string;
  usedBy: string[];
  virtualizationName: string;
  virtualizationDescription: string;
  i18nLockPopoverHeading: string;
  i18nLockPopover: string;
  i18nSecuredText: string;
  secured: boolean;
}

export const VirtualizationListItem: React.FunctionComponent<IVirtualizationListItemProps> = props => {
  const [labelType, setLabelType] = React.useState(props.labelType);
  const [publishStateText, setPublishStateText] = React.useState(
    props.i18nPublishState
  );

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
  }, [props.i18nPublishState, props.currentPublishedState]);

  React.useEffect(() => {
    setLabelType(props.labelType);
  }, [props.labelType]);

  return (
    <DataListItem
      aria-labelledby={'single-action-item1'}
      data-testid={`virtualization-list-item-${toValidHtmlId(
        props.virtualizationName
      )}-list-item`}
      className={'virtualization-list-item'}
    >
      <DataListItemRow>
        <DataListItemCells
          dataListCells={[
            <DataListCell width={1} key={0}>
              {props.icon ? (
                <div className={'virtualization-list-item__icon-wrapper'}>
                  <img
                    src={props.icon}
                    alt={props.virtualizationName}
                    width={46}
                  />
                </div>
              ) : (
                <CubeIcon size={'lg'} />
              )}
            </DataListCell>,
            <DataListCell key={'primary content'} width={4}>
              <div className={'virtualization-list-item__text-wrapper'}>
                <b data-testid={'virtualization-list-item-name'}>
                  {props.virtualizationName}
                </b>
                <br />
                <p data-testid={'virtualization-list-item-description'}>
                  {props.virtualizationDescription
                    ? props.virtualizationDescription
                    : ''}
                </p>
              </div>
            </DataListCell>,
            <DataListCell key={'used-by content'} width={4}>
              <div
                className={'virtualization-list-item__used-by'}
                data-testid={'virtualization-list-item__used-by'}
              >
                {props.usedBy.length > 0 ? (
                  props.i18nInUseText
                ) : (
                  <span className={'virtualization-list-item__usedby-text'} />
                )}
              </div>
            </DataListCell>,
            <DataListCell key={'odata_content'} width={4}>
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
            </DataListCell>,
            <DataListCell key={'data_permission'} width={1}>
              {props.secured && (
                <Popover
                  headerContent={<div>{props.i18nLockPopoverHeading}</div>}
                  bodyContent={<div>{props.i18nLockPopover}</div>}
                >
                  <Split className={'virtualization-list-item-secured'}>
                    <SplitItem>
                      <LockIcon className={'virtualization-list-item-lock-icon'} />
                    </SplitItem>
                    <SplitItem className={'virtualization-list-item-secured-text'}>{props.i18nSecuredText}</SplitItem>
                  </Split>
                </Popover>
              )}
            </DataListCell>,
          ]}
        />
        <DataListAction
          aria-labelledby={'actions'}
          id={'actions'}
          aria-label={'Actions'}
        >
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
          {props.dropdownActions}
        </DataListAction>
      </DataListItemRow>
    </DataListItem>
  );
};
