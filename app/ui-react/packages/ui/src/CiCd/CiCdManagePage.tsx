import * as React from 'react';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  SimplePageHeader,
} from '../Shared';
import { CiCdEditDialog } from './CiCdEditDialog';
import { CiCdList } from './CiCdList';
import { CiCdListEmptyState } from './CiCdListEmptyState';
import { CiCdListItem } from './CiCdListItem';
import { CiCdListView, ICiCdListViewProps } from './CiCdListView';
import { ICiCdListPageItem } from './CiCdUIModels';

export interface ICiCdListPageProps extends ICiCdListViewProps {
  i18nAddTagDialogTitle: string;
  i18nAddTagDialogDescription: string;
  i18nEditTagDialogTitle: string;
  i18nEditTagDialogDescription: string;
  i18nTagInputLabel: string;
  i18nSaveButtonText: string;
  i18nCancelButtonText: string;
  i18nRemoveButtonText: string;
  i18nConfirmCancelButtonText: string;
  i18nConfirmRemoveButtonText: string;
  i18nEditButtonText: string;
  i18nEmptyStateTitle: string;
  i18nRemoveConfirmationMessage: (name: string) => string;
  i18nRemoveConfirmationTitle: string;
  i18nRemoveConfirmationDetailMessage: string;
  i18nPageTitle: string;
  i18nPageDescription: string;
  listItems: ICiCdListPageItem[];
  onEditItem: (oldName: string, newName: string) => void;
  onAddItem: (name: string) => void;
  onRemoveItem: (name: string) => void;
}

export interface ICiCdListPageState {
  showAddDialog: boolean;
  showEditDialog: boolean;
  showRemoveDialog: boolean;
  currentItemName?: string;
}

export class CiCdManagePage extends React.Component<
  ICiCdListPageProps,
  ICiCdListPageState
> {
  public constructor(props: ICiCdListPageProps) {
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
    this.setState({ showAddDialog: true });
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
      <div className="container-pf-nav-pf-vertical">
        <SimplePageHeader
          i18nTitle={this.props.i18nPageTitle}
          i18nDescription={this.props.i18nPageDescription}
        />
        {this.state.showAddDialog && (
          <CiCdEditDialog
            i18nTitle={this.props.i18nAddTagDialogTitle}
            i18nDescription={this.props.i18nAddTagDialogDescription}
            tagName={''}
            i18nInputLabel={this.props.i18nTagInputLabel}
            i18nSaveButtonText={this.props.i18nSaveButtonText}
            i18nCancelButtonText={this.props.i18nCancelButtonText}
            onHide={this.closeAddDialog}
            onSave={this.handleSave}
          />
        )}
        {this.state.showEditDialog && (
          <CiCdEditDialog
            i18nTitle={this.props.i18nEditTagDialogTitle}
            i18nDescription={this.props.i18nEditTagDialogDescription}
            tagName={this.state.currentItemName!}
            i18nInputLabel={this.props.i18nTagInputLabel}
            i18nSaveButtonText={this.props.i18nSaveButtonText}
            i18nCancelButtonText={this.props.i18nCancelButtonText}
            onHide={this.closeEditDialog}
            onSave={this.handleSave}
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
          children={
            <>
              {this.props.listItems.length !== 0 && (
                <CiCdList
                  children={this.props.listItems.map((listItem, index) => (
                    <CiCdListItem
                      key={index}
                      onEditClicked={this.openEditDialog}
                      onRemoveClicked={this.openRemoveDialog}
                      i18nEditButtonText={this.props.i18nEditButtonText}
                      i18nRemoveButtonText={this.props.i18nRemoveButtonText}
                      name={listItem.name}
                      i18nUsesText={listItem.i18nUsesText}
                    />
                  ))}
                />
              )}
              {this.props.listItems.length === 0 && (
                <CiCdListEmptyState
                  i18nTitle={this.props.i18nEmptyStateTitle}
                  i18nAddNewButtonText={this.props.i18nAddNewButtonText}
                />
              )}
            </>
          }
        />
      </div>
    );
  }
}
