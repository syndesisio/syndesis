import {
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Tooltip
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink } from '../../Layout';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
} from '../../Shared';
import './ExtensionListItem.css';

export interface IExtensionListItemProps {
  detailsPageLink: H.LocationDescriptor;
  extensionDescription?: string;
  extensionIcon: React.ReactNode;
  extensionId: string;
  extensionName: string;
  i18nCancelText: string;
  i18nDelete: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nDeleteTip?: string;
  i18nDetails: string;
  i18nDetailsTip?: string;
  i18nExtensionType: string;
  i18nUpdate: string;
  i18nUpdateTip?: string;
  i18nUsedByMessage: string;

  /**
   * An href to use when the extension is being updated.
   */
  linkUpdateExtension: H.LocationDescriptor;

  onDelete: (extensionId: string) => void;
  usedBy: number;
}

export interface IExtensionListItemState {
  showDeleteDialog: boolean;
}

export const ExtensionListItem: React.FunctionComponent<
  IExtensionListItemProps
> = props => {
  const [showDeleteDialog, setShowDeleteDialog] = React.useState(false);

  const doCancel = () => {
    setShowDeleteDialog(false);
  };

  const doDelete = () => {
    setShowDeleteDialog(false);

    // TODO: disable components while delete is processing
    props.onDelete(props.extensionId);
  };

  const showConfirmationDialog = () => {
    setShowDeleteDialog(true);
  };

  return (
    <>
      <ConfirmationDialog
        // extensionId={this.props.extensionId}
        buttonStyle={ConfirmationButtonStyle.DANGER}
        i18nCancelButtonText={props.i18nCancelText}
        i18nConfirmButtonText={props.i18nDelete}
        i18nConfirmationMessage={props.i18nDeleteModalMessage}
        i18nTitle={props.i18nDeleteModalTitle}
        icon={ConfirmationIconType.DANGER}
        showDialog={showDeleteDialog}
        onCancel={doCancel}
        onConfirm={doDelete}
      />
      <DataListItem aria-labelledby={'extension list item'}
                    className={'extension-list-item'}
                    data-testid={`extension-list-item-${toValidHtmlId(
                      props.extensionName
                    )}-list-item`}
      >
        <DataListItemRow>
          <DataListItemCells
            dataListCells={[
              <DataListCell width={1} key={0} className={'extension-list-item__icon-wrapper'}>
                {props.extensionIcon}
              </DataListCell>,
              <DataListCell key={'primary content'} width={4}>
                <div className={'extension-list-item__text-wrapper'}>
                  <b>{props.extensionName}</b><br/>
                  {
                    props.extensionDescription ? props.extensionDescription : ''
                  }
                </div>
              </DataListCell>,

              <DataListCell key={'type content'} width={3} className={'extension-list-item__type'}>
                {props.i18nExtensionType}
              </DataListCell>,
              <DataListCell key={'used by content'} width={3} className={'extension-list-item__used-by'}>
                {props.i18nUsedByMessage}
              </DataListCell>
            ]}
          />
          <DataListAction
            aria-labelledby={'extension list actions'}
            id={'extension-list-action'}
            aria-label={'Actions'}
            className={'extension-list-item__actions'}
          >
            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'detailsTip'}>
                  {props.i18nDetailsTip
                    ? props.i18nDetailsTip
                    : props.i18nDetails}
                </div>
              }
            >
              <ButtonLink
                data-testid={'extension-list-item-details-button'}
                href={props.detailsPageLink}
                as={'default'}
              >
                {props.i18nDetails}
              </ButtonLink>
            </Tooltip>

            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'updateTip'}>
                  {props.i18nUpdateTip ? props.i18nUpdateTip : props.i18nUpdate}
                </div>
              }
            >
              <ButtonLink
                data-testid={'extension-list-item-update-button'}
                href={props.linkUpdateExtension}
                as={'default'}
              >
                {props.i18nUpdate}
              </ButtonLink>
            </Tooltip>

            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'deleteTip'}>
                  {props.i18nDeleteTip
                    ?props.i18nDeleteTip
                    : props.i18nDelete}
                </div>
              }
            >
              <ButtonLink
                data-testid={'extension-list-item-delete-button'}
                disabled={props.usedBy !== 0}
                onClick={showConfirmationDialog}
              >
                {props.i18nDelete}
              </ButtonLink>
            </Tooltip>
          </DataListAction>
        </DataListItemRow>
      </DataListItem>
    </>
  );
};
