import * as H from '@syndesis/history';
import {
  Button,
  ListViewInfoItem,
  ListViewItem,
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

  const getDeleteTooltip = (): JSX.Element => {
    return (
      <Tooltip id="deleteTip">
        {props.i18nDeleteTip ? props.i18nDeleteTip : props.i18nDelete}
      </Tooltip>
    );
  };

  const getDetailsTooltip = (): JSX.Element => {
    return (
      <Tooltip id="detailsTip">
        {props.i18nDetailsTip ? props.i18nDetailsTip : props.i18nDetails}
      </Tooltip>
    );
  };

  const getUpdateTooltip = (): JSX.Element => {
    return (
      <Tooltip id="updateTip">
        {props.i18nUpdateTip ? props.i18nUpdateTip : props.i18nUpdate}
      </Tooltip>
    );
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
      <ListViewItem
        data-testid={`extension-list-item-${toValidHtmlId(
          props.extensionName
        )}-list-item`}
        actions={
          <>
            <OverlayTrigger overlay={getDetailsTooltip()} placement="top">
              <ButtonLink
                data-testid={'extension-list-item-details-button'}
                href={props.detailsPageLink}
                as={'default'}
              >
                {props.i18nDetails}
              </ButtonLink>
            </OverlayTrigger>
            <OverlayTrigger overlay={getUpdateTooltip()} placement="top">
              <ButtonLink
                data-testid={'extension-list-item-update-button'}
                href={props.linkUpdateExtension}
                as={'default'}
              >
                {props.i18nUpdate}
              </ButtonLink>
            </OverlayTrigger>
            <OverlayTrigger overlay={getDeleteTooltip()} placement="top">
              <Button
                data-testid={'extension-list-item-delete-button'}
                bsStyle="default"
                disabled={props.usedBy !== 0}
                onClick={showConfirmationDialog}
              >
                {props.i18nDelete}
              </Button>
            </OverlayTrigger>
          </>
        }
        additionalInfo={[
          <ListViewInfoItem key={1}>
            {props.i18nExtensionType}
          </ListViewInfoItem>,
          <ListViewInfoItem key={2}>
            {props.i18nUsedByMessage}
          </ListViewInfoItem>,
        ]}
        description={
          props.extensionDescription ? props.extensionDescription : ''
        }
        heading={props.extensionName}
        hideCloseIcon={true}
        leftContent={props.extensionIcon}
        stacked={true}
      />
    </>
  );
};
