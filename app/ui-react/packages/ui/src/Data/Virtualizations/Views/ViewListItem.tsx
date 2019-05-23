import * as H from '@syndesis/history';
import {
  DropdownKebab,
  ListViewIcon,
  ListViewItem,
  MenuItem,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';
import { ButtonLink } from '../../../Layout';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
} from '../../../Shared';

export interface IViewListItemProps {
  viewDescription: string;
  viewIcon?: string;
  viewName: string;
  viewEditPageLink: H.LocationDescriptor;
  i18nCancelText: string;
  i18nDelete: string;
  i18nDeleteTip?: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nEdit: string;
  i18nEditTip?: string;
  onDelete: (viewName: string) => void;
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
          buttonStyle={ConfirmationButtonStyle.DANGER}
          i18nCancelButtonText={this.props.i18nCancelText}
          i18nConfirmButtonText={this.props.i18nDelete}
          i18nConfirmationMessage={this.props.i18nDeleteModalMessage}
          i18nTitle={this.props.i18nDeleteModalTitle}
          icon={ConfirmationIconType.DANGER}
          showDialog={this.state.showDeleteDialog}
          onCancel={this.handleCancel}
          onConfirm={this.handleDelete}
        />
        <ListViewItem
          data-testid={`view-list-item-${toValidHtmlId(
            this.props.viewName
          )}-list-item`}
          actions={
            <div className="form-group">
              <OverlayTrigger overlay={this.getEditTooltip()} placement="top">
                <ButtonLink
                  data-testid={'view-list-item-edit-button'}
                  href={this.props.viewEditPageLink}
                  as={'default'}
                >
                  {this.props.i18nEdit}
                </ButtonLink>
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
              <ListViewIcon name={'table'} />
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
