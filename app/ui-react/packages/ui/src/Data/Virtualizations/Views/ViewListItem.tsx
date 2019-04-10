import {
  Button,
  DropdownKebab,
  ListViewIcon,
  ListViewItem,
  MenuItem,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';

export interface IViewListItemProps {
  viewDescription: string;
  viewIcon?: string;
  viewName: string;
  i18nDelete: string;
  i18nDeleteTip?: string;
  i18nEdit: string;
  i18nEditTip?: string;
  onDelete: (viewName: string) => void;
  onEdit: (viewName: string) => void;
}

export class ViewListItem extends React.Component<IViewListItemProps> {
  public render() {
    return (
      <ListViewItem
        actions={
          <div className="form-group">
            <OverlayTrigger overlay={this.getEditTooltip()} placement="top">
              <Button bsStyle="default" onClick={this.handleEdit}>
                {this.props.i18nEdit}
              </Button>
            </OverlayTrigger>
            <DropdownKebab
              id={`view-${this.props.viewName}-action-menu`}
              pullRight={true}
            >
              <OverlayTrigger
                overlay={this.getDeleteTooltip()}
                placement="left"
              >
                <MenuItem onClick={this.handleDelete}>Delete</MenuItem>
              </OverlayTrigger>
            </DropdownKebab>
          </div>
        }
        heading={this.props.viewName}
        description={
          this.props.viewDescription ? this.props.viewDescription : ''
        }
        hideCloseIcon={true}
        leftContent={
          this.props.viewIcon ? (
            <div className="blank-slate-pf-icon">
              <img
                src={this.props.viewIcon}
                alt={this.props.viewName}
                width={46}
              />
            </div>
          ) : (
            <ListViewIcon name={'cube'} />
          )
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

  private getEditTooltip() {
    return (
      <Tooltip id="editTip">
        {this.props.i18nEditTip ? this.props.i18nEditTip : this.props.i18nEdit}
      </Tooltip>
    );
  }

  private handleEdit = () => {
    if (this.props.viewName) {
      this.props.onEdit(this.props.viewName);
    }
  };

  private handleDelete = () => {
    if (this.props.viewName) {
      this.props.onDelete(this.props.viewName);
    }
  };
}
