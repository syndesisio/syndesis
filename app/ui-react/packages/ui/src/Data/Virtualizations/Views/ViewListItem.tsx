import { Tooltip } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import {
  DropdownKebab,
  ListViewIcon,
  ListViewInfoItem,
  ListViewItem,
  MenuItem,
} from 'patternfly-react';
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
  i18nDeleteTip?: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nEdit: string;
  i18nEditTip?: string;
  i18nInvalid: string;
  isValid: boolean;
  onDelete: (viewId: string, viewName: string) => void;
}

export const ViewListItem: React.FunctionComponent<
  IViewListItemProps
> = props => {
  const [showDeleteDialog, setShowDeleteDialog] = React.useState(false);

  const doCancel = () => {
    setShowDeleteDialog(false);
  }

  const doDelete = () => {
    setShowDeleteDialog(false);

    // TODO: disable components while delete is processing
    props.onDelete(props.viewId, props.viewName);
  }

  const showConfirmationDialog = () => {
    setShowDeleteDialog(true);
  }

  return (
    <>
      <ConfirmationDialog
        buttonStyle={ConfirmationButtonStyle.DANGER}
        i18nCancelButtonText={props.i18nCancelText}
        i18nConfirmButtonText={props.i18nDelete}
        i18nConfirmationMessage={props.i18nDeleteModalMessage}
        i18nTitle={props.i18nDeleteModalTitle}
        icon={ConfirmationIconType.DANGER}
        showDialog={showDeleteDialog}
        onCancel={doCancel}
        onConfirm={doDelete}
      />
      <ListViewItem
        data-testid={`view-list-item-${toValidHtmlId(
          props.viewName
        )}-list-item`}
        actions={
          <div className="form-group">
            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'editTip'}>
                  {props.i18nEditTip ? props.i18nEditTip : props.i18nEdit}
                </div>
              }
            >
              <ButtonLink
                data-testid={'view-list-item-edit-button'}
                href={props.viewEditPageLink}
                as={'default'}
              >
                {props.i18nEdit}
              </ButtonLink>
            </Tooltip>
            <DropdownKebab
              id={`view-${props.viewName}-action-menu`}
              pullRight={true}
            >
              <Tooltip
                position={'left'}
                enableFlip={true}
                content={
                  <div id={'deleteTip'}>
                    {props.i18nDeleteTip
                      ? props.i18nDeleteTip
                      : props.i18nDelete}
                  </div>
                }
              >
                <MenuItem onClick={showConfirmationDialog}>
                  {props.i18nDelete}
                </MenuItem>
              </Tooltip>
            </DropdownKebab>
          </div>
        }
        additionalInfo={
          props.isValid
            ? []
            : [
                <ListViewInfoItem key={1}>
                  <div className={'view-list-item__invalidView'}>
                    {props.i18nInvalid}
                  </div>
                </ListViewInfoItem>,
              ]
        }
        heading={props.viewName}
        description={props.viewDescription ? props.viewDescription : ''}
        hideCloseIcon={true}
        leftContent={
          props.viewIcon ? (
            <div className="blank-slate-pf-icon">
              <img src={props.viewIcon} alt={props.viewName} width={46} />
            </div>
          ) : (
            <ListViewIcon name={'table'} />
          )
        }
        stacked={true}
      />
    </>
  );

}
