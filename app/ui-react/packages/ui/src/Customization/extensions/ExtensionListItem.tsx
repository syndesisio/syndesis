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
  extensionIcon?: string;
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

export class ExtensionListItem extends React.Component<
  IExtensionListItemProps,
  IExtensionListItemState
> {
  public constructor(props: IExtensionListItemProps) {
    super(props);

    this.state = {
      showDeleteDialog: false, // initial visibility of delete dialog
    };

    this.doCancel = this.doCancel.bind(this);
    this.doDelete = this.doDelete.bind(this);
    this.showDeleteDialog = this.showDeleteDialog.bind(this);
  }

  public doCancel() {
    this.setState({
      showDeleteDialog: false, // hide dialog
    });
  }

  public doDelete() {
    this.setState({
      showDeleteDialog: false, // hide dialog
    });

    // TODO: disable components while delete is processing
    this.props.onDelete(this.props.extensionId);
  }

  public getDeleteTooltip() {
    return (
      <Tooltip id="deleteTip">
        {this.props.i18nDeleteTip
          ? this.props.i18nDeleteTip
          : this.props.i18nDelete}
      </Tooltip>
    );
  }

  public getDetailsTooltip() {
    return (
      <Tooltip id="detailsTip">
        {this.props.i18nDetailsTip
          ? this.props.i18nDetailsTip
          : this.props.i18nDetails}
      </Tooltip>
    );
  }

  public getUpdateTooltip() {
    return (
      <Tooltip id="updateTip">
        {this.props.i18nUpdateTip
          ? this.props.i18nUpdateTip
          : this.props.i18nUpdate}
      </Tooltip>
    );
  }

  public showDeleteDialog() {
    this.setState({
      showDeleteDialog: true,
    });
  }

  public render() {
    return (
      <>
        <ConfirmationDialog
          // extensionId={this.props.extensionId}
          buttonStyle={ConfirmationButtonStyle.DANGER}
          i18nCancelButtonText={this.props.i18nCancelText}
          i18nConfirmButtonText={this.props.i18nDelete}
          i18nConfirmationMessage={this.props.i18nDeleteModalMessage}
          i18nTitle={this.props.i18nDeleteModalTitle}
          icon={ConfirmationIconType.DANGER}
          showDialog={this.state.showDeleteDialog}
          onCancel={this.doCancel}
          onConfirm={this.doDelete}
        />
        <ListViewItem
          data-testid={`extension-list-item-${toValidHtmlId(
            this.props.extensionName
          )}-list-item`}
          actions={
            <div className="form-group">
              <OverlayTrigger
                overlay={this.getDetailsTooltip()}
                placement="top"
              >
                <ButtonLink
                  data-testid={'extension-list-item-details-button'}
                  href={this.props.detailsPageLink}
                  as={'default'}
                >
                  {this.props.i18nDetails}
                </ButtonLink>
              </OverlayTrigger>
              <OverlayTrigger overlay={this.getUpdateTooltip()} placement="top">
                <ButtonLink
                  data-testid={'extension-list-item-update-button'}
                  href={this.props.linkUpdateExtension}
                  as={'default'}
                >
                  {this.props.i18nUpdate}
                </ButtonLink>
              </OverlayTrigger>
              <OverlayTrigger overlay={this.getDeleteTooltip()} placement="top">
                <Button
                  data-testid={'extension-list-item-delete-button'}
                  bsStyle="default"
                  disabled={this.props.usedBy !== 0}
                  onClick={this.showDeleteDialog}
                >
                  {this.props.i18nDelete}
                </Button>
              </OverlayTrigger>
            </div>
          }
          additionalInfo={[
            <ListViewInfoItem key={1}>
              {this.props.i18nExtensionType}
            </ListViewInfoItem>,
            <ListViewInfoItem key={2}>
              {this.props.i18nUsedByMessage}
            </ListViewInfoItem>,
          ]}
          description={
            this.props.extensionDescription
              ? this.props.extensionDescription
              : ''
          }
          heading={this.props.extensionName}
          hideCloseIcon={true}
          leftContent={
            this.props.extensionIcon ? (
              <div className="blank-slate-pf-icon">
                <img
                  src={this.props.extensionIcon}
                  alt={this.props.extensionName}
                  width={46}
                />
              </div>
            ) : null
          }
          stacked={true}
        />
      </>
    );
  }
}
