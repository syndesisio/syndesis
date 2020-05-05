import * as React from 'react';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  IListViewToolbarProps,
  SimplePageHeader,
} from '../../Shared';
import { CiCdEditDialog } from './CiCdEditDialog';
import { CiCdListView } from './CiCdListView';
import { TagNameValidationError } from './CiCdUIModels';

export interface ICiCdManagePageUIChildrenProps {
  openAddDialog: () => void;
  openEditDialog: (name: string) => void;
  openRemoveDialog: (name: string) => void;
}

export interface ICiCdManagePageUIProps extends IListViewToolbarProps {
  i18nAddTagDialogTitle: string;
  i18nAddTagDialogDescription: string;
  i18nEditTagDialogTitle: string;
  i18nEditTagDialogDescription: string;
  i18nAddNewButtonText: string;
  i18nTagInputLabel: string;
  i18nSaveButtonText: string;
  i18nCancelButtonText: string;
  i18nConfirmCancelButtonText: string;
  i18nConfirmRemoveButtonText: string;
  i18nRemoveConfirmationMessage: (name: string) => string;
  i18nRemoveConfirmationTitle: string;
  i18nRemoveConfirmationDetailMessage: string;
  i18nPageTitle: string;
  i18nPageDescription: string;
  i18nNoNameError: string;
  i18nNameInUseError: string;
  nameValidationError: TagNameValidationError;
  onValidateItem: (name: string) => void;
  onEditItem: (oldName: string, newName: string) => void;
  onCancelEditItem: () => void;
  onAddItem: (name: string) => void;
  onCancelAddItem: () => void;
  onRemoveItem: (name: string) => void;
  children: (props: ICiCdManagePageUIChildrenProps) => any;
}

export const CiCdManagePageUI: React.FunctionComponent<ICiCdManagePageUIProps> = props => {
  const [showAddDialog, setShowAddDialog] = React.useState(false);
  const [showEditDialog, setShowEditDialog] = React.useState(false);
  const [showRemoveDialog, setShowRemoveDialog] = React.useState(false);
  const [currentItemName, setCurrentItemName] = React.useState<string | undefined>();

  const handleSave = (name: string) => {
    if (showEditDialog) {
      closeEditDialog();
      props.onEditItem(currentItemName!, name);
    }
    if (showAddDialog) {
      closeAddDialog();
      props.onAddItem(name);
    }
    if (showRemoveDialog) {
      closeRemoveDialog();
      props.onRemoveItem(name);
    }
  };
  const handleRemoveConfirm = () => {
    handleSave(currentItemName!);
  };
  const openRemoveDialog = (name: string) => {
    setCurrentItemName(name);
    setShowRemoveDialog(true);
  };
  const closeRemoveDialog = () => {
    setShowRemoveDialog(false);
  };
  const openAddDialog = () => {
    setCurrentItemName(name);
    setShowAddDialog(true);
  };
  const closeAddDialog = () => {
    setShowAddDialog(false);
  };
  const openEditDialog = (name: string) => {
    setCurrentItemName(name);
    setShowEditDialog(true);
  };
  const closeEditDialog = () => {
    setShowEditDialog(false);
  };
  return (
    <>
      <SimplePageHeader
        i18nTitle={props.i18nPageTitle}
        i18nDescription={props.i18nPageDescription}
      />
      {showAddDialog && (
        <CiCdEditDialog
          key={currentItemName}
          i18nTitle={props.i18nAddTagDialogTitle}
          i18nDescription={props.i18nAddTagDialogDescription}
          tagName={currentItemName!}
          i18nInputLabel={props.i18nTagInputLabel}
          i18nSaveButtonText={props.i18nSaveButtonText}
          i18nCancelButtonText={props.i18nCancelButtonText}
          i18nNoNameError={props.i18nNoNameError}
          i18nNameInUseError={props.i18nNameInUseError}
          validationError={props.nameValidationError}
          onHide={() => {
            closeAddDialog();
            props.onCancelAddItem();
          }}
          onSave={handleSave}
          onValidate={props.onValidateItem}
        />
      )}
      {showEditDialog && (
        <CiCdEditDialog
          key={currentItemName}
          i18nTitle={props.i18nEditTagDialogTitle}
          i18nDescription={props.i18nEditTagDialogDescription}
          tagName={currentItemName!}
          i18nInputLabel={props.i18nTagInputLabel}
          i18nSaveButtonText={props.i18nSaveButtonText}
          i18nCancelButtonText={props.i18nCancelButtonText}
          i18nNoNameError={props.i18nNoNameError}
          i18nNameInUseError={props.i18nNameInUseError}
          validationError={props.nameValidationError}
          onHide={() => {
            closeEditDialog();
            props.onCancelEditItem();
          }}
          onSave={handleSave}
          onValidate={props.onValidateItem}
        />
      )}
      {showRemoveDialog && (
        <ConfirmationDialog
          buttonStyle={ConfirmationButtonStyle.NORMAL}
          icon={ConfirmationIconType.DANGER}
          i18nCancelButtonText={props.i18nConfirmCancelButtonText}
          i18nConfirmButtonText={props.i18nConfirmRemoveButtonText}
          i18nConfirmationMessage={props.i18nRemoveConfirmationMessage(
            currentItemName!
          )}
          i18nTitle={props.i18nRemoveConfirmationTitle}
          i18nDetailsMessage={props.i18nRemoveConfirmationDetailMessage}
          showDialog={showRemoveDialog}
          onCancel={closeRemoveDialog}
          onConfirm={handleRemoveConfirm}
        />
      )}
      <CiCdListView
        activeFilters={props.activeFilters}
        currentFilterType={props.currentFilterType}
        currentSortType={props.currentSortType}
        currentValue={props.currentValue}
        filterTypes={props.filterTypes}
        isSortAscending={props.isSortAscending}
        resultsCount={props.resultsCount}
        sortTypes={props.sortTypes}
        onUpdateCurrentValue={props.onUpdateCurrentValue}
        onValueKeyPress={props.onValueKeyPress}
        onFilterAdded={props.onFilterAdded}
        onSelectFilterType={props.onSelectFilterType}
        onFilterValueSelected={props.onFilterValueSelected}
        onRemoveFilter={props.onRemoveFilter}
        onClearFilters={props.onClearFilters}
        onToggleCurrentSortDirection={props.onToggleCurrentSortDirection}
        onUpdateCurrentSortType={props.onUpdateCurrentSortType}
        i18nResultsCount={props.i18nResultsCount}
        i18nAddNewButtonText={props.i18nAddNewButtonText}
        onAddNew={openAddDialog}
      >
        {props.children({
          openAddDialog,
          openEditDialog,
          openRemoveDialog,
        })}
      </CiCdListView>
    </>
  );
};
