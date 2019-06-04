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

export interface IApiConnectorListItemProps {
  apiConnectorDescription?: string;
  apiConnectorId: string;
  apiConnectorIcon?: string;
  apiConnectorName: string;
  detailsPageLink: H.LocationDescriptor;
  i18nCancelLabel: string;
  i18nDelete: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nDeleteTip?: string;
  i18nDetails: string;
  i18nDetailsTip?: string;
  i18nUsedByMessage: string;
  onDelete: (apiConnectorId: string) => void;
  usedBy: number;
}

export interface IApiConnectorListItemState {
  showDeleteDialog: boolean;
}

export class ApiConnectorListItem extends React.Component<
  IApiConnectorListItemProps,
  IApiConnectorListItemState
> {
  public constructor(props: IApiConnectorListItemProps) {
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
    this.props.onDelete(this.props.apiConnectorId);
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

  public showDeleteDialog() {
    this.setState({
      showDeleteDialog: true,
    });
  }

  public render() {
    return (
      <>
        <ConfirmationDialog
          buttonStyle={ConfirmationButtonStyle.DANGER}
          i18nCancelButtonText={this.props.i18nCancelLabel}
          i18nConfirmButtonText={this.props.i18nDelete}
          i18nConfirmationMessage={this.props.i18nDeleteModalMessage}
          i18nTitle={this.props.i18nDeleteModalTitle}
          icon={ConfirmationIconType.DANGER}
          showDialog={this.state.showDeleteDialog}
          onCancel={this.doCancel}
          onConfirm={this.doDelete}
        />
        <ListViewItem
          data-testid={`api-connector-list-item-${toValidHtmlId(
            this.props.apiConnectorName
          )}-list-item`}
          actions={
            <>
              <OverlayTrigger
                overlay={this.getDetailsTooltip()}
                placement="top"
              >
                <ButtonLink
                  data-testid={'api-connector-list-item-details-button'}
                  href={this.props.detailsPageLink}
                  as={'default'}
                >
                  {this.props.i18nDetails}
                </ButtonLink>
              </OverlayTrigger>
              <OverlayTrigger overlay={this.getDeleteTooltip()} placement="top">
                <Button
                  data-testid={'api-connector-list-item-delete-button'}
                  bsStyle="default"
                  disabled={this.props.usedBy !== 0}
                  onClick={this.showDeleteDialog}
                >
                  {this.props.i18nDelete}
                </Button>
              </OverlayTrigger>
            </>
          }
          additionalInfo={[
            <ListViewInfoItem key={1}>
              {this.props.i18nUsedByMessage}
            </ListViewInfoItem>,
          ]}
          description={
            this.props.apiConnectorDescription
              ? this.props.apiConnectorDescription
              : ''
          }
          heading={this.props.apiConnectorName}
          hideCloseIcon={true}
          leftContent={
            this.props.apiConnectorIcon ? (
              <div className="blank-slate-pf-icon">
                <img
                  src={this.props.apiConnectorIcon}
                  alt={this.props.apiConnectorName}
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
