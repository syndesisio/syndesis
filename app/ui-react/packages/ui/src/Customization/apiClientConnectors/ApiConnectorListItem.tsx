import * as H from '@syndesis/history';
import {
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow
} from '@patternfly/react-core';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink } from '../../Layout';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
} from '../../Shared';
import './ApiConnectorListItem.css';

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
        <DataListItem aria-labelledby={'single-action-item1'}
                      data-testid={`api-connector-list-item-${toValidHtmlId(this.props.apiConnectorName)}-list-item`}
                      className={'api-connector-list-item'}
        >
          <DataListItemRow>
            <DataListItemCells
              dataListCells={[
                <DataListCell width={1} key={0}>
                  {this.props.apiConnectorIcon ? (
                    <div className={'api-connector-list-item__icon-wrapper'}>
                      <img
                        src={this.props.apiConnectorIcon}
                        alt={this.props.apiConnectorName}
                        width={46}
                      />
                    </div>
                  ) : null}
                </DataListCell>,
                <DataListCell key={'primary content'} width={4}>
                  <div className={'api-connector-list-item__text-wrapper'}>
                    <b>{this.props.apiConnectorName}</b><br/>
                    {
                      this.props.apiConnectorDescription
                        ? this.props.apiConnectorDescription
                        : ''
                    }
                  </div>
                </DataListCell>,
                <DataListCell key={'secondary content'} width={4}>
                  <div className={'api-connector-list-item__used-by'}>
                    {this.props.i18nUsedByMessage}
                  </div>
                </DataListCell>
              ]}
            />
            <DataListAction
              aria-labelledby={'single-action-item1 single-action-action1'}
              id={'single-action-action1'}
              aria-label={'Actions'}
            >
              <ButtonLink
                data-testid={'api-connector-list-item-details-button'}
                href={this.props.detailsPageLink}
                as={'default'}
              >
                {this.props.i18nDetails}
              </ButtonLink>
              <ButtonLink
                data-testid={'api-connector-list-item-delete-button'}
                disabled={this.props.usedBy !== 0}
                onClick={this.showDeleteDialog}
              >
                {this.props.i18nDelete}
              </ButtonLink>
            </DataListAction>
          </DataListItemRow>
        </DataListItem>
      </>
    );
  }
}
