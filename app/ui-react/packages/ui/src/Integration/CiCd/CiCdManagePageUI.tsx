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

export interface ICiCdManagePageUIState {
  showAddDialog: boolean;
  showEditDialog: boolean;
  showRemoveDialog: boolean;
  currentItemName?: string;
}

export class CiCdManagePageUI extends React.Component<
  ICiCdManagePageUIProps,
  ICiCdManagePageUIState
> {
  public constructor(props: ICiCdManagePageUIProps) {
    super(props);
    this.state = {
      showAddDialog: false,
      showEditDialog: false,
      showRemoveDialog: false,
    };
    this.openAddDialog = this.openAddDialog.bind(this);
    this.closeAddDialog = this.closeAddDialog.bind(this);
    this.openEditDialog = this.openEditDialog.bind(this);
    this.closeEditDialog = this.closeEditDialog.bind(this);
    this.openRemoveDialog = this.openRemoveDialog.bind(this);
    this.closeRemoveDialog = this.closeRemoveDialog.bind(this);
    this.handleSave = this.handleSave.bind(this);
    this.handleRemoveConfirm = this.handleRemoveConfirm.bind(this);
  }
  public handleSave(name: string) {
    if (this.state.showEditDialog) {
      this.closeEditDialog();
      this.props.onEditItem(this.state.currentItemName!, name);
    }
    if (this.state.showAddDialog) {
      this.closeAddDialog();
      this.props.onAddItem(name);
    }
    if (this.state.showRemoveDialog) {
      this.closeRemoveDialog();
      this.props.onRemoveItem(name);
    }
  }
  public handleRemoveConfirm() {
    this.handleSave(this.state.currentItemName!);
  }
  public openRemoveDialog(name: string) {
    this.setState({ currentItemName: name, showRemoveDialog: true });
  }
  public closeRemoveDialog() {
    this.setState({ showRemoveDialog: false });
  }
  public openAddDialog() {
    this.setState({ currentItemName: '', showAddDialog: true });
  }
  public closeAddDialog() {
    this.setState({ showAddDialog: false });
  }
  public openEditDialog(name: string) {
    this.setState({ currentItemName: name, showEditDialog: true });
  }
  public closeEditDialog() {
    this.setState({ showEditDialog: false });
  }
  public render() {
    return (
      <>
        <SimplePageHeader
          i18nTitle={this.props.i18nPageTitle}
          i18nDescription={this.props.i18nPageDescription}
        />
        {this.state.showAddDialog && (
          <CiCdEditDialog
            key={this.state.currentItemName}
            i18nTitle={this.props.i18nAddTagDialogTitle}
            i18nDescription={this.props.i18nAddTagDialogDescription}
            tagName={this.state.currentItemName!}
            i18nInputLabel={this.props.i18nTagInputLabel}
            i18nSaveButtonText={this.props.i18nSaveButtonText}
            i18nCancelButtonText={this.props.i18nCancelButtonText}
            i18nNoNameError={this.props.i18nNoNameError}
            i18nNameInUseError={this.props.i18nNameInUseError}
            validationError={this.props.nameValidationError}
            onHide={() => {
              this.closeAddDialog();
              this.props.onCancelAddItem();
            }}
            onSave={this.handleSave}
            onValidate={this.props.onValidateItem}
          />
        )}
        {this.state.showEditDialog && (
          <CiCdEditDialog
            key={this.state.currentItemName}
            i18nTitle={this.props.i18nEditTagDialogTitle}
            i18nDescription={this.props.i18nEditTagDialogDescription}
            tagName={this.state.currentItemName!}
            i18nInputLabel={this.props.i18nTagInputLabel}
            i18nSaveButtonText={this.props.i18nSaveButtonText}
            i18nCancelButtonText={this.props.i18nCancelButtonText}
            i18nNoNameError={this.props.i18nNoNameError}
            i18nNameInUseError={this.props.i18nNameInUseError}
            validationError={this.props.nameValidationError}
            onHide={() => {
              this.closeEditDialog();
              this.props.onCancelEditItem();
            }}
            onSave={this.handleSave}
            onValidate={this.props.onValidateItem}
          />
        )}
        {this.state.showRemoveDialog && (
          <ConfirmationDialog
            buttonStyle={ConfirmationButtonStyle.NORMAL}
            icon={ConfirmationIconType.DANGER}
            i18nCancelButtonText={this.props.i18nConfirmCancelButtonText}
            i18nConfirmButtonText={this.props.i18nConfirmRemoveButtonText}
            i18nConfirmationMessage={this.props.i18nRemoveConfirmationMessage(
              this.state.currentItemName!
            )}
            i18nTitle={this.props.i18nRemoveConfirmationTitle}
            i18nDetailsMessage={this.props.i18nRemoveConfirmationDetailMessage}
            showDialog={this.state.showRemoveDialog}
            onCancel={this.closeRemoveDialog}
            onConfirm={this.handleRemoveConfirm}
          />
        )}
        <CiCdListView
          activeFilters={this.props.activeFilters}
          currentFilterType={this.props.currentFilterType}
          currentSortType={this.props.currentSortType}
          currentValue={this.props.currentValue}
          filterTypes={this.props.filterTypes}
          isSortAscending={this.props.isSortAscending}
          resultsCount={this.props.resultsCount}
          sortTypes={this.props.sortTypes}
          onUpdateCurrentValue={this.props.onUpdateCurrentValue}
          onValueKeyPress={this.props.onValueKeyPress}
          onFilterAdded={this.props.onFilterAdded}
          onSelectFilterType={this.props.onSelectFilterType}
          onFilterValueSelected={this.props.onFilterValueSelected}
          onRemoveFilter={this.props.onRemoveFilter}
          onClearFilters={this.props.onClearFilters}
          onToggleCurrentSortDirection={this.props.onToggleCurrentSortDirection}
          onUpdateCurrentSortType={this.props.onUpdateCurrentSortType}
          i18nResultsCount={this.props.i18nResultsCount}
          i18nAddNewButtonText={this.props.i18nAddNewButtonText}
          onAddNew={this.openAddDialog}
        >
          {this.props.children({
            openAddDialog: this.openAddDialog,
            openEditDialog: this.openEditDialog,
            openRemoveDialog: this.openRemoveDialog,
          })}
        </CiCdListView>
      </>
    );
  }
}
