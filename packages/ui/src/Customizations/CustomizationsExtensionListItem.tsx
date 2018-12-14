import {
  Button,
  ListViewInfoItem,
  ListViewItem,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';

export interface IExtensionListItemProps {
  extensionDescription?: string;
  extensionIcon?: string;
  extensionId: string;
  i18nDelete: string;
  i18nDeleteTip?: string;
  i18nDetails: string;
  i18nDetailsTip?: string;
  i18nExtensionType: string;
  i18nUpdate: string;
  i18nUpdateTip?: string;
  i18nUsedByMessage: string;
  onDelete: (extensionId: string) => void;
  onDetails: (extensionId: string) => void;
  onUpdate: (extensionId: string) => void;
  usedBy: number;
}

export class CustomizationsExtensionListItem extends React.Component<
  IExtensionListItemProps
> {
  public render() {
    return (
      <ListViewItem
        actions={
          <div className="form-group">
            <OverlayTrigger overlay={this.getDetailsTooltip()} placement="top">
              <Button bsStyle="default" onClick={this.handleDetails}>
                {this.props.i18nDetails}
              </Button>
            </OverlayTrigger>
            <OverlayTrigger overlay={this.getUpdateTooltip()} placement="top">
              <Button bsStyle="default" onClick={this.handleUpdate}>
                {this.props.i18nUpdate}
              </Button>
            </OverlayTrigger>
            <OverlayTrigger overlay={this.getDeleteTooltip()} placement="top">
              <Button
                bsStyle="default"
                disabled={this.props.usedBy !== 0}
                onClick={this.handleDelete}
              >
                {this.props.i18nDelete}
              </Button>
            </OverlayTrigger>
          </div>
        }
        additionalInfo={
          <ListViewInfoItem>{this.props.i18nExtensionType}</ListViewInfoItem>
        }
        description={
          this.props.extensionDescription ? this.props.extensionDescription : ''
        }
        heading={this.props.extensionId}
        hideCloseIcon={true}
        leftContent={
          this.props.extensionIcon ? (
            <div className="blank-slate-pf-icon">
              <img
                src={this.props.extensionIcon}
                alt={this.props.extensionId}
                width={46}
              />
            </div>
          ) : null
        }
        stacked={false}
      />
    );
  }

  private getDeleteTooltip() {
    return (
      <Tooltip id="deleteTip">
        {this.props.i18nDeleteTip
          ? this.props.i18nDeleteTip
          : this.props.i18nDelete}
      </Tooltip>
    );
  }

  private getDetailsTooltip() {
    return (
      <Tooltip id="detailsTip">
        {this.props.i18nDetailsTip
          ? this.props.i18nDetailsTip
          : this.props.i18nDetails}
      </Tooltip>
    );
  }

  private getUpdateTooltip() {
    return (
      <Tooltip id="updateTip">
        {this.props.i18nUpdateTip
          ? this.props.i18nUpdateTip
          : this.props.i18nUpdate}
      </Tooltip>
    );
  }

  private handleDelete = () => {
    this.props.onDelete(this.props.extensionId);
  };

  private handleDetails = () => {
    this.props.onDetails(this.props.extensionId);
  };

  private handleUpdate = () => {
    this.props.onUpdate(this.props.extensionId);
  };
}
