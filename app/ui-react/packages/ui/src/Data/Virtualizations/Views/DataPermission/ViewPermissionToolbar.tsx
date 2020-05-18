import {
  Button,
  ButtonVariant,
  Chip,
  ChipGroup,
  Dropdown,
  DropdownItem,
  DropdownToggle,
  DropdownToggleCheckbox,
  InputGroup,
  Pagination,
  Stack,
  StackItem,
  TextInput,
  Title,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
  ToolbarSection,
} from '@patternfly/react-core';
import { SortAlphaDownIcon, SortAlphaUpIcon } from '@patternfly/react-icons';
import * as React from 'react';
import {
  IActiveFilter,
  IFilterType,
  IFilterValue,
  IListViewToolbarProps,
  ISortType,
} from '../../../../Shared';
import './ViewPermissionToolbar.css';

export interface IViewPermissionToolbarProps extends IListViewToolbarProps {
  page: number;
  perPage: number;
  allPageViewsSelected: boolean;
  enableClearPermissions: boolean;
  enableSetPermissions: boolean;
  setPage: (page: number) => void;
  setPerPage: (perPage: number) => void;
  clearViewSelection: () => void;
  selectAllViews: () => void;
  selectPageViews: () => void;
  handleSetModalToggle: () => void;
  handleClearModalToggle: () => void;
  i18nSelectNone: string;
  i18nSelectPage: string;
  i18nSelectAll: string;
  i18nSetPermission: string;
  i18nClearPermission: string;
  i18nClearFilters: string;
}

export const ViewPermissionToolbar: React.FunctionComponent<IViewPermissionToolbarProps> = ({
  page,
  perPage,
  allPageViewsSelected,
  enableClearPermissions,
  enableSetPermissions,
  setPage,
  setPerPage,
  clearViewSelection,
  selectAllViews,
  selectPageViews,
  handleSetModalToggle,
  handleClearModalToggle,
  activeFilters,
  currentFilterType,
  currentSortType,
  currentValue,
  filterTypes,
  isSortAscending,
  resultsCount,
  sortTypes,
  i18nResultsCount,
  i18nSelectNone,
  i18nSelectPage,
  i18nSelectAll,
  i18nSetPermission,
  i18nClearPermission,
  i18nClearFilters,
  onUpdateCurrentSortType,
  onUpdateCurrentValue,
  onFilterValueSelected,
  onSelectFilterType,
  onValueKeyPress,
  onRemoveFilter,
  onClearFilters,
  onToggleCurrentSortDirection,
}) => {
  /**
   * React useState Hook to handle state in component.
   */
  const [isMultiSelectOpen, setIsMultiSelectOpen] = React.useState<boolean>(
    false
  );

  const [sortTypeOpen, setSortTypeOpen] = React.useState(false);

  const [filterTypeOpen, setFilterTypeOpen] = React.useState(false);

  const [filterValueOpen, setFilterValueOpen] = React.useState(false);

  const handleSelectSortType = (sortType: ISortType) => {
    setSortTypeOpen(false);
    onUpdateCurrentSortType(sortType);
  };
  const handleSelectFilterType = (filterType: IFilterType) => {
    setFilterTypeOpen(false);
    onSelectFilterType(filterType);
  };
  const handleSelectFilterValue = (filterValue: IFilterValue) => {
    setFilterValueOpen(false);
    onFilterValueSelected(filterValue);
  };

  const onSetPage = (event: any, pageNumber: number) => {
    setPage(pageNumber);
  };

  const onPerPageSelect = (event: any, perPageNumber: number) => {
    setPerPage(perPageNumber);
  };

  const multiSelectToggle = (isOpen: boolean) => {
    setIsMultiSelectOpen(isOpen);
  };

  const onMultiSelect = () => {
    setIsMultiSelectOpen(!isMultiSelectOpen);
  };

  const multiSelectDropdownItems = [
    <DropdownItem key="select-none" onClick={clearViewSelection}>
      {i18nSelectNone}
    </DropdownItem>,
    <DropdownItem key="select-page-list" onClick={selectPageViews}>{i18nSelectPage}</DropdownItem>,
    <DropdownItem key="-select-all-list" onClick={selectAllViews}>
      {i18nSelectAll}
    </DropdownItem>,
  ];

  const onToolbarCheckSelection = (checked: boolean, event: any) => {
    if (checked) {
      selectPageViews()
    } else {
      clearViewSelection()
    }
  };

  const renderInput = () => {
    if (!currentFilterType) {
      return null;
    }
    if (currentFilterType.filterType === 'select') {
      return (
        <Dropdown
          toggle={
            <DropdownToggle
              id={'filter-value-selector-toggle'}
              // tslint:disable-next-line: jsx-no-lambda
              onToggle={() => setFilterValueOpen(!filterValueOpen)}
            >
              Add {currentFilterType.title} filter
            </DropdownToggle>
          }
          isOpen={filterValueOpen}
          dropdownItems={
            currentFilterType.filterValues &&
            currentFilterType.filterValues!.map(
              (
                filterValue: IFilterValue,
                index: string | number | undefined
              ) => (
                <DropdownItem
                  key={index}
                  // tslint:disable-next-line: jsx-no-lambda
                  onClick={() => handleSelectFilterValue(filterValue)}
                >
                  {filterValue.title}
                </DropdownItem>
              )
            )
          }
        />
      );
    } else {
      return (
        <TextInput
          id={'toolbar-filter-text-input'}
          type={currentFilterType.filterType}
          value={currentValue}
          placeholder={currentFilterType.placeholder}
          // tslint:disable-next-line: jsx-no-lambda
          onChange={(val: any, event: any) =>
            onUpdateCurrentValue(event as any)
          }
          // tslint:disable-next-line: jsx-no-lambda
          onKeyPress={(event: any) => onValueKeyPress(event as any)}
        />
      );
    }
  };

  return (
    <Toolbar className="pf-l-toolbar pf-u-justify-content-space-between pf-u-mx-xl pf-u-my-md view-permission-toolbar">
      <ToolbarGroup>
        <ToolbarItem className="pf-u-mr-xl">
          <Dropdown
            onSelect={onMultiSelect}
            toggle={
              <DropdownToggle
                id="stacked-example-toggle"
                splitButtonItems={[
                  <DropdownToggleCheckbox
                    id="example-checkbox-1"
                    key="split-checkbox"
                    aria-label="Select all"
                    checked={allPageViewsSelected}
                    // tslint:disable-next-line: jsx-no-lambda
                    onChange={(checked: boolean, event: any) => onToolbarCheckSelection(checked,event)}
                  />,
                ]}
                onToggle={multiSelectToggle}
              />
            }
            isOpen={isMultiSelectOpen}
            dropdownItems={multiSelectDropdownItems}
          />
        </ToolbarItem>
        <ToolbarItem className="pf-u-mr-xl">
          <InputGroup>
            {filterTypes && filterTypes.length > 1 && (
              <Dropdown
                toggle={
                  <DropdownToggle
                    id={'toolbar-filter-type-selector-toggle'}
                    // tslint:disable-next-line: jsx-no-lambda
                    onToggle={() => setFilterTypeOpen(!filterTypeOpen)}
                  >
                    {currentFilterType.title}
                  </DropdownToggle>
                }
                isOpen={filterTypeOpen}
                dropdownItems={filterTypes.map((filterType, index) => (
                  <>
                    {filterType.id !== currentFilterType.id && (
                      <DropdownItem
                        key={index}
                        // tslint:disable-next-line: jsx-no-lambda
                        onClick={() => handleSelectFilterType(filterType)}
                      >
                        {filterType.title}
                      </DropdownItem>
                    )}
                  </>
                ))}
              />
            )}
            {renderInput()}
          </InputGroup>
        </ToolbarItem>
        {sortTypes && sortTypes.length > 1 && (
          <ToolbarItem className="pf-u-mr-md">
            <Dropdown
              toggle={
                <DropdownToggle
                  id={'toolbar-sort-type-selector-toggle'}
                  // tslint:disable-next-line: jsx-no-lambda
                  onToggle={() => setSortTypeOpen(!sortTypeOpen)}
                >
                  {currentSortType.title}
                </DropdownToggle>
              }
              isOpen={sortTypeOpen}
              dropdownItems={sortTypes.map((sortType, index) => (
                <>
                  {sortType.id !== currentSortType.id && (
                    <DropdownItem
                      key={index}
                      // tslint:disable-next-line: jsx-no-lambda
                      onClick={() => handleSelectSortType(sortType)}
                    >
                      {sortType.title}
                    </DropdownItem>
                  )}
                </>
              ))}
            />
          </ToolbarItem>
        )}
        <ToolbarItem className="pf-u-mr-md">
          <Button
            variant={ButtonVariant.plain}
            onClick={onToggleCurrentSortDirection}
          >
            {isSortAscending && <SortAlphaDownIcon />}
            {!isSortAscending && <SortAlphaUpIcon />}
          </Button>
        </ToolbarItem>
        <ToolbarItem className="pf-u-mr-md">
          <Button
            variant="primary"
            onClick={handleSetModalToggle}
            isDisabled={!enableSetPermissions}
          >
            {i18nSetPermission}
          </Button>
        </ToolbarItem>
        <ToolbarItem className="pf-u-mr-md">
          <Button
            variant="secondary"
            onClick={handleClearModalToggle}
            isDisabled={!enableClearPermissions}
          >
            {i18nClearPermission}
          </Button>
        </ToolbarItem>
      </ToolbarGroup>
      <ToolbarGroup>
        <Pagination
          itemCount={resultsCount}
          perPage={perPage}
          page={page}
          onSetPage={onSetPage}
          widgetId="pagination-options-menu-top"
          onPerPageSelect={onPerPageSelect}
        />
      </ToolbarGroup>
      <ToolbarSection aria-label={'Results Section'}>
        <ToolbarGroup>
          <ToolbarItem>
            {activeFilters && activeFilters.length > 0 && (
              <>
                <Title size={'lg'}>{i18nResultsCount}</Title>
                <Stack>
                  <StackItem>
                    <Title size={'md'}>Active Filters:</Title>
                  </StackItem>
                  <StackItem>
                    <ChipGroup>
                      {activeFilters.map((item: IActiveFilter, index) => (
                        // tslint:disable-next-line: jsx-no-lambda
                        <Chip
                          key={index}
                          // tslint:disable-next-line: jsx-no-lambda
                          onClick={() => onRemoveFilter(item)}
                        >
                          {item.title} = {item.value}
                        </Chip>
                      ))}
                    </ChipGroup>
                  </StackItem>
                  <StackItem>
                    <Button
                      variant={ButtonVariant.link}
                      data-testid={'list-view-toolbar-clear-filters'}
                      // tslint:disable-next-line: jsx-no-lambda
                      onClick={event => onClearFilters(event as any)}
                    >
                      {i18nClearFilters}
                    </Button>
                  </StackItem>
                </Stack>
              </>
            )}
          </ToolbarItem>
        </ToolbarGroup>
      </ToolbarSection>
    </Toolbar>
  );
};
