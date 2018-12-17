import {
  Button,
  ListViewIcon,
  ListViewInfoItem,
  ListViewItem,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';

export interface IApiConnectorListItemProps {
  apiConnectorDescription?: string;
  apiConnectorId: string;
  apiConnectorName: string;
  apiConnectorIcon?: string;
  i18nDelete: string;
  i18nDeleteTip?: string;
  i18nDetails: string;
  i18nDetailsTip?: string;
  i18nUsedByMessage: string;
  onDelete: (apiConnectorId: string) => void;
  onDetails: (extensionId: string) => void;
  usedBy: number;
}

export class CustomizationsApiConnectorListItem extends React.Component<
  IApiConnectorListItemProps
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
          <ListViewInfoItem>{this.props.i18nUsedByMessage}</ListViewInfoItem>
        }
        description={
          this.props.apiConnectorDescription
            ? this.props.apiConnectorDescription
            : ''
        }
        heading={this.props.apiConnectorName}
        hideCloseIcon={true}
        leftContent={
          this.props.apiConnectorIcon ? <ListViewIcon name={'gear'} /> : null
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

  private handleDelete = () => {
    this.props.onDelete(this.props.apiConnectorId);
  };

  private handleDetails = () => {
    this.props.onDetails(this.props.apiConnectorId);
  };
}
