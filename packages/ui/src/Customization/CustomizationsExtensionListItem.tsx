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
  extensionId?: string;
  extensionName: string;
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
  public constructor(props: IExtensionListItemProps) {
    super(props);
    this.handleDelete = this.handleDelete.bind(this);
    this.handleDetails = this.handleDetails.bind(this);
    this.handleUpdate = this.handleUpdate.bind(this);
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

  public handleDelete() {
    if (this.props.extensionId) {
      this.props.onDelete(this.props.extensionId);
    }
  }

  public handleDetails() {
    if (this.props.extensionId) {
      this.props.onDetails(this.props.extensionId);
    }
  }

  public handleUpdate() {
    if (this.props.extensionId) {
      this.props.onUpdate(this.props.extensionId);
    }
  }

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
                disabled={this.props.extensionId && this.props.usedBy !== 0}
                onClick={this.handleDelete}
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
        ]}
        description={
          this.props.extensionDescription ? this.props.extensionDescription : ''
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
        stacked={false}
      />
    );
  }
}
