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

  const getDeleteTooltip = (): JSX.Element => {
    return (
      <Tooltip id="deleteTip">
        {props.i18nDeleteTip ? props.i18nDeleteTip : props.i18nDelete}
      </Tooltip>
    );
  }

  const getEditTooltip = (): JSX.Element => {
    return (
      <Tooltip id="editTip">
        {props.i18nEditTip ? props.i18nEditTip : props.i18nEdit}
      </Tooltip>
    );
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
            <OverlayTrigger overlay={getEditTooltip()} placement="top">
              <ButtonLink
                data-testid={'view-list-item-edit-button'}
                href={props.viewEditPageLink}
                as={'default'}
              >
                {props.i18nEdit}
              </ButtonLink>
            </OverlayTrigger>
            <DropdownKebab
              id={`view-${props.viewName}-action-menu`}
              pullRight={true}
            >
              <OverlayTrigger
                overlay={getDeleteTooltip()}
                placement="left"
              >
                <MenuItem onClick={showConfirmationDialog}>
                  {props.i18nDelete}
                </MenuItem>
              </OverlayTrigger>
            </DropdownKebab>
          </div>
        }
        heading={props.viewName}
        description={
          props.viewDescription ? props.viewDescription : ''
        }
        hideCloseIcon={true}
        leftContent={
          props.viewIcon ? (
            <div className="blank-slate-pf-icon">
              <img
                src={props.viewIcon}
                alt={props.viewName}
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
