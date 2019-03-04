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
import { DeleteConfirmationDialog } from '../../Shared';

export interface IVirtListItemProps {
  i18nCancelText: string;
  i18nDelete: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nDraft: string;
  i18nDraftTip: string;
  i18nEdit: string;
  i18nEditTip?: string;
  i18nExport: string;
  i18nPublished: string;
  i18nPublishedTip: string;
  i18nUnpublish: string;
  i18nPublish: string;
  icon?: string;
  isPublished: boolean;
  onDelete: (virtName: string) => void;
  onEdit: (virtName: string) => void;
  onExport: (virtName: string) => void;
  onPublish: (virtName: string) => void;
  onUnpublish: (virtName: string) => void;
  virtName: string;
  virtDescription: string;
}

export interface IVirtListItemState {
  showDeleteDialog: boolean;
}

export class VirtListItem extends React.Component<
  IVirtListItemProps,
  IVirtListItemState
> {
  public constructor(props: IVirtListItemProps) {
    super(props);
    this.state = {
      showDeleteDialog: false, // initial visibility of delete dialog
    };
    this.handleCancel = this.handleCancel.bind(this);
    this.handleDelete = this.handleDelete.bind(this);
    this.handleEdit = this.handleEdit.bind(this);
    this.handleExport = this.handleExport.bind(this);
    this.handleUnpublish = this.handleUnpublish.bind(this);
    this.handlePublish = this.handlePublish.bind(this);
    this.showDeleteDialog = this.showDeleteDialog.bind(this);
  }

  public getEditTooltip() {
    return (
      <Tooltip id="detailsTip">
        {this.props.i18nEditTip ? this.props.i18nEditTip : this.props.i18nEdit}
      </Tooltip>
    );
  }

  public getPublishedTooltip() {
    return (
      <Tooltip id="detailsTip">
        {this.props.isPublished
          ? this.props.i18nPublishedTip
          : this.props.i18nDraftTip}
      </Tooltip>
    );
  }

  public handleCancel() {
    this.setState({
      showDeleteDialog: false, // hide dialog
    });
  }

  public handleDelete() {
    this.setState({
      showDeleteDialog: false, // hide dialog
    });

    // TODO: disable components while delete is processing
    if (this.props.virtName) {
      this.props.onDelete(this.props.virtName);
    }
  }

  public handleEdit() {
    if (this.props.virtName) {
      this.props.onEdit(this.props.virtName);
    }
  }

  public handleExport() {
    if (this.props.virtName) {
      this.props.onExport(this.props.virtName);
    }
  }

  public handlePublish() {
    if (this.props.virtName) {
      this.props.onPublish(this.props.virtName);
    }
  }

  public handleUnpublish() {
    if (this.props.virtName) {
      this.props.onUnpublish(this.props.virtName);
    }
  }

  public showDeleteDialog() {
    this.setState({
      showDeleteDialog: true,
    });
  }

  public render() {
    const publishStyle = this.props.isPublished ? 'success' : 'default';
    return (
      <>
        <DeleteConfirmationDialog
          i18nCancelButtonText={this.props.i18nCancelText}
          i18nDeleteButtonText={this.props.i18nDelete}
          i18nDeleteMessage={this.props.i18nDeleteModalMessage}
          i18nTitle={this.props.i18nDeleteModalTitle}
          showDialog={this.state.showDeleteDialog}
          onCancel={this.handleCancel}
          onDelete={this.handleDelete}
        />
        <ListViewItem
          actions={
            <div className="form-group">
              <OverlayTrigger
                overlay={this.getPublishedTooltip()}
                placement="top"
              >
                <Button bsStyle={publishStyle} variant={publishStyle}>
                  {this.props.isPublished
                    ? this.props.i18nPublished
                    : this.props.i18nDraft}
                </Button>
              </OverlayTrigger>
              <OverlayTrigger overlay={this.getEditTooltip()} placement="top">
                <Button bsStyle="default" onClick={this.handleEdit}>
                  {this.props.i18nEdit}
                </Button>
              </OverlayTrigger>
              <DropdownKebab
                id={`virtualization-${this.props.virtName}-action-menu`}
                pullRight={true}
              >
                <MenuItem onClick={this.showDeleteDialog}>
                  {this.props.i18nDelete}
                </MenuItem>
                <MenuItem onClick={this.handleExport}>
                  {this.props.i18nExport}
                </MenuItem>
                <MenuItem
                  onClick={
                    this.props.isPublished
                      ? this.handleUnpublish
                      : this.handlePublish
                  }
                >
                  {this.props.isPublished
                    ? this.props.i18nUnpublish
                    : this.props.i18nPublish}
                </MenuItem>
              </DropdownKebab>
            </div>
          }
          heading={this.props.virtName}
          description={
            this.props.virtDescription ? this.props.virtDescription : ''
          }
          hideCloseIcon={true}
          leftContent={
            this.props.icon ? (
              <div className="blank-slate-pf-icon">
                <img
                  src={this.props.icon}
                  alt={this.props.virtName}
                  width={46}
                />
              </div>
            ) : (
              <ListViewIcon name={'database'} />
            )
          }
          stacked={true}
        />
      </>
    );
  }
}
