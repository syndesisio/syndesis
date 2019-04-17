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
import { ConfirmationDialog, ConfirmationDialogType } from '../../../Shared';

export interface IViewListItemProps {
  viewDescription: string;
  viewIcon?: string;
  viewName: string;
  i18nCancelText: string;
  i18nDelete: string;
  i18nDeleteTip?: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nEdit: string;
  i18nEditTip?: string;
  onDelete: (viewName: string) => void;
  onEdit: (viewName: string) => void;
}

export interface IViewListItemState {
  showDeleteDialog: boolean;
}

export class ViewListItem extends React.Component<
  IViewListItemProps,
  IViewListItemState
> {
  public constructor(props: IViewListItemProps) {
    super(props);
    this.state = {
      showDeleteDialog: false, // initial visibility of delete dialog
    };
    this.handleCancel = this.handleCancel.bind(this);
    this.handleDelete = this.handleDelete.bind(this);
    this.showDeleteDialog = this.showDeleteDialog.bind(this);
  }

  public render() {
    return (
      <>
        <ConfirmationDialog
          confirmationType={ConfirmationDialogType.DANGER}
          i18nCancelButtonText={this.props.i18nCancelText}
          i18nAcceptButtonText={this.props.i18nDelete}
          i18nConfirmationMessage={this.props.i18nDeleteModalMessage}
          i18nTitle={this.props.i18nDeleteModalTitle}
          showDialog={this.state.showDeleteDialog}
          onCancel={this.handleCancel}
          onAccept={this.handleDelete}
        />
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
                  <MenuItem onClick={this.showDeleteDialog}>
                    {this.props.i18nDelete}
                  </MenuItem>
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
      </>
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

  private handleCancel() {
    this.setState({
      showDeleteDialog: false, // hide dialog
    });
  }

  private handleEdit = () => {
    if (this.props.viewName) {
      this.props.onEdit(this.props.viewName);
    }
  };

  private showDeleteDialog() {
    this.setState({
      showDeleteDialog: true,
    });
  }

  private handleDelete() {
    this.setState({
      showDeleteDialog: false, // hide dialog
    });

    // TODO: disable components while delete is processing
    if (this.props.viewName) {
      this.props.onDelete(this.props.viewName);
    }
  }
}
