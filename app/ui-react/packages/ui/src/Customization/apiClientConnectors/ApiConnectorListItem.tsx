import {
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Tooltip
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { useState } from 'react';
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

export const ApiConnectorListItem: React.FC<
  IApiConnectorListItemProps
> = (
  {
    apiConnectorDescription,
    apiConnectorId,
    apiConnectorIcon,
    apiConnectorName,
    detailsPageLink,
    i18nCancelLabel,
    i18nDelete,
    i18nDeleteModalMessage,
    i18nDeleteModalTitle,
    i18nDeleteTip,
    i18nDetails,
    i18nDetailsTip,
    i18nUsedByMessage,
    onDelete,
    usedBy
  }) => {
  // initial visibility of delete dialog
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const doCancel = () => {
    setShowDeleteDialog(false);
  };

  const doDelete = () => {
    setShowDeleteDialog(false);

    // TODO: disable components while delete is processing
    onDelete(apiConnectorId);
  };

  const showDeleteDialogAction = () => {
    setShowDeleteDialog(true);
  };

  return (
    <>
      <ConfirmationDialog
        buttonStyle={ConfirmationButtonStyle.DANGER}
        i18nCancelButtonText={i18nCancelLabel}
        i18nConfirmButtonText={i18nDelete}
        i18nConfirmationMessage={i18nDeleteModalMessage}
        i18nTitle={i18nDeleteModalTitle}
        icon={ConfirmationIconType.DANGER}
        showDialog={showDeleteDialog}
        onCancel={doCancel}
        onConfirm={doDelete}
      />
      <DataListItem aria-labelledby={'single-action-item1'}
                    data-testid={`api-connector-list-item-${toValidHtmlId(apiConnectorName)}-list-item`}
                    className={'api-connector-list-item'}
      >
        <DataListItemRow>
          <DataListItemCells
            dataListCells={[
              <DataListCell width={1} key={0}>
                {apiConnectorIcon ? (
                  <div className={'api-connector-list-item__icon-wrapper'}>
                    <img
                      data-testid={'api-connector-icon'}
                      src={apiConnectorIcon}
                      alt={apiConnectorName}
                      width={46}
                    />
                  </div>
                ) : null}
              </DataListCell>,
              <DataListCell key={'primary content'} width={4}>
                <div className={'api-connector-list-item__text-wrapper'}>
                  <b data-testid={'api-connector-name'}>{apiConnectorName}</b><br/>
                  <span data-testid={'api-connector-description'}>{
                    apiConnectorDescription
                      ? apiConnectorDescription
                      : ''
                  }</span>
                </div>
              </DataListCell>,
              <DataListCell key={'secondary content'} width={4}>
                <div 
                  className={'api-connector-list-item__used-by'}
                  data-testid={'api-connector-used-by'}
                >
                  {i18nUsedByMessage}
                </div>
              </DataListCell>
            ]}
          />
          <DataListAction
            aria-labelledby={'single-action-item1 single-action-action1'}
            id={'single-action-action1'}
            aria-label={'Actions'}
          >
            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'detailsTip'}>
                  {i18nDetailsTip
                    ? i18nDetailsTip
                    : i18nDetails}
                </div>
              }
            >
              <ButtonLink
                data-testid={'api-connector-list-item-details-button'}
                href={detailsPageLink}
                as={'default'}
              >
                {i18nDetails}
              </ButtonLink>
            </Tooltip>

            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'deleteTip'}>
                  {i18nDeleteTip
                    ?i18nDeleteTip
                    : i18nDelete}
                </div>
              }
            >
              <ButtonLink
                data-testid={'api-connector-list-item-delete-button'}
                disabled={usedBy !== 0}
                onClick={showDeleteDialogAction}
              >
                {i18nDelete}
              </ButtonLink>
            </Tooltip>
          </DataListAction>
        </DataListItemRow>
      </DataListItem>
    </>
  );

};
