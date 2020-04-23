import {
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Dropdown,
  DropdownItem,
  DropdownPosition,
  KebabToggle,
  Tooltip,
} from '@patternfly/react-core';
import { TableIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';
import { ButtonLink } from '../../../Layout';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
} from '../../../Shared';
import './ViewListItem.css';

export interface IViewListItemProps {
  viewDescription: string;
  viewIcon?: string;
  viewId: string;
  viewName: string;
  viewEditPageLink: H.LocationDescriptor;
  i18nCancelText: string;
  i18nDelete: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nEdit: string;
  i18nEditTip?: string;
  i18nInvalid: string;
  isValid: boolean;
  onDelete: (viewId: string, viewName: string) => void;
}

export const ViewListItem: React.FC<IViewListItemProps> = ({
  viewDescription,
  viewIcon,
  viewId,
  viewName,
  viewEditPageLink,
  i18nCancelText,
  i18nDelete,
  i18nDeleteModalMessage,
  i18nDeleteModalTitle,
  i18nEdit,
  i18nEditTip,
  i18nInvalid,
  isValid,
  onDelete,
}) => {
  const [showDeleteDialog, setShowDeleteDialog] = React.useState(false);
  const [isOpen, setOpen] = React.useState(false);

  const doCancel = () => {
    setShowDeleteDialog(false);
  };

  const doDelete = () => {
    setShowDeleteDialog(false);

    // TODO: disable components while delete is processing
    onDelete(viewId, viewName);
  };

  const showConfirmationDialog = () => {
    setShowDeleteDialog(true);
  };

  const onSelect = () => {
    setOpen(!isOpen);
  };

  const onToggle = (open: boolean) => {
    setOpen(open);
  };

  return (
    <>
      <ConfirmationDialog
        buttonStyle={ConfirmationButtonStyle.DANGER}
        i18nCancelButtonText={i18nCancelText}
        i18nConfirmButtonText={i18nDelete}
        i18nConfirmationMessage={i18nDeleteModalMessage}
        i18nTitle={i18nDeleteModalTitle}
        icon={ConfirmationIconType.DANGER}
        showDialog={showDeleteDialog}
        onCancel={doCancel}
        onConfirm={doDelete}
      />
      <DataListItem
        aria-labelledby={'view list item'}
        data-testid={`view-list-item-${toValidHtmlId(viewName)}-list-item`}
        className={'view-list-item'}
      >
        <DataListItemRow>
          <DataListItemCells
            dataListCells={[
              <DataListCell width={1} key={0}>
                {viewIcon ? (
                  <div className={'view-list-item__icon-wrapper'}>
                    <img src={viewIcon} alt={viewName} width={46} />
                  </div>
                ) : (
                  <TableIcon size={'lg'} />
                )}
              </DataListCell>,
              <DataListCell key={'primary content'} width={4}>
                <div className={'view-list-item__text-wrapper'}>
                  <b>{viewName}</b>
                  <br />
                  {viewDescription ? viewDescription : ''}
                </div>
              </DataListCell>,
              <DataListCell key={'secondary content'} width={4}>
                <div className={'view-list-item__invalid-view'} data-testid={'view-list-item-invalid-view'}>
                  {isValid ? '' : i18nInvalid}
                </div>
              </DataListCell>,
            ]}
          />
          <DataListAction
            aria-labelledby={'view list actions'}
            id={'view-list-actions'}
            aria-label={'Actions'}
          >
            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'editTip'}>{i18nEditTip ? i18nEditTip : i18nEdit}</div>
              }
            >
              <ButtonLink
                data-testid={'view-list-item-edit-button'}
                href={viewEditPageLink}
                as={'default'}
              >
                {i18nEdit}
              </ButtonLink>
            </Tooltip>
            <Dropdown
              isPlain={true}
              position={DropdownPosition.right}
              isOpen={isOpen}
              onSelect={onSelect}
              toggle={<KebabToggle onToggle={onToggle} />}
              dropdownItems={[
                <DropdownItem
                  data-testid={'view-list-item-delete-action'}
                  key={'deleteView'}
                  onClick={showConfirmationDialog}
                >
                  {i18nDelete}
                </DropdownItem>,
              ]}
            />
          </DataListAction>
        </DataListItemRow>
      </DataListItem>
    </>
  );
};
