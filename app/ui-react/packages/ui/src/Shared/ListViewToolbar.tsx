import {
  Button,
  ButtonVariant,
  Chip,
  ChipGroup,
  Dropdown,
  DropdownItem,
  DropdownToggle,
  InputGroup,
  Stack,
  StackItem,
  TextInput,
  Title,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
} from '@patternfly/react-core';
import { SortAlphaDownIcon, SortAlphaUpIcon } from '@patternfly/react-icons';
import * as React from 'react';
import './ListViewToolbar.css';

export interface IFilterValue {
  id: string;
  title: string;
}

export interface IFilterType {
  id: string;
  filterType: 'select' | 'text' | 'number';
  filterValues?: IFilterValue[];
  placeholder: string;
  title: string;
}

export interface ISortType {
  id: string;
  isNumeric: boolean;
  title: string;
}

export interface IActiveFilter {
  id: string;
  title: string;
  value: string;
}

export interface IListViewToolbarProps {
  activeFilters: IActiveFilter[];
  filterTypes: IFilterType[];
  currentFilterType: IFilterType;
  currentSortType: ISortType;
  currentValue: any;
  isSortAscending: boolean;
  resultsCount: number;
  sortTypes: ISortType[];
  i18nResultsCount: string;

  onUpdateCurrentValue(event: Event): void;

  onValueKeyPress(keyEvent: KeyboardEvent): void;

  onFilterAdded(id: string, title: string, value: string): void;

  onSelectFilterType(filterType: IFilterType): void;

  onFilterValueSelected(filterValue: { id: string; title: string }): void;

  onRemoveFilter(filter: IActiveFilter): void;

  onClearFilters(event: React.MouseEvent<HTMLAnchorElement>): void;

  onToggleCurrentSortDirection(): void;

  onUpdateCurrentSortType(sortType: ISortType): void;
}

export const ListViewToolbar: React.FunctionComponent<IListViewToolbarProps> =
  ({
    activeFilters,
    children,
    currentFilterType,
    currentSortType,
    currentValue,
    filterTypes,
    isSortAscending,
    resultsCount,
    sortTypes,
    i18nResultsCount,
    onUpdateCurrentSortType,
    onUpdateCurrentValue,
    onFilterAdded,
    onFilterValueSelected,
    onSelectFilterType,
    onValueKeyPress,
    onRemoveFilter,
    onClearFilters,
    onToggleCurrentSortDirection,
  }) => {
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
                onToggle={() => setFilterValueOpen(!filterValueOpen)}
              >
                Add {currentFilterType.title} filter
              </DropdownToggle>
            }
            isOpen={filterValueOpen}
            dropdownItems={
              currentFilterType.filterValues &&
              currentFilterType.filterValues!.map((filterValue, index) => (
                <DropdownItem
                  key={index}
                  onClick={() => handleSelectFilterValue(filterValue)}
                >
                  {filterValue.title}
                </DropdownItem>
              ))
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
            onChange={(val, event) => onUpdateCurrentValue(event as any)}
            onKeyPress={(event) => onValueKeyPress(event as any)}
          />
        );
      }
    };
    return (
      <Toolbar className="pf-l-toolbar pf-u-justify-content-space-between pf-u-mx-xl pf-u-my-md">
        <ToolbarGroup>
          <ToolbarItem className="pf-u-mr-xl">
            <InputGroup>
              {filterTypes && filterTypes.length > 1 && (
                <Dropdown
                  toggle={
                    <DropdownToggle
                      id={'toolbar-filter-type-selector-toggle'}
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
        </ToolbarGroup>
        <ToolbarGroup>
          <ToolbarItem className={'pf-u-mx-md list-view-toolbar__child-items'}>
            {children}
          </ToolbarItem>
        </ToolbarGroup>

        <ToolbarGroup aria-label={'Results Section'}>
          <ToolbarItem>
            {activeFilters && activeFilters.length > 0 && (
              <>
                <Title size={'lg'} headingLevel={'h4'}>
                  {i18nResultsCount}
                </Title>
                <Stack>
                  <StackItem>
                    <Title size={'md'} headingLevel={'h5'}>
                      Active Filters:
                    </Title>
                  </StackItem>
                  <StackItem>
                    <ChipGroup>
                      {activeFilters.map((item: IActiveFilter, index) => (
                        <Chip key={index} onClick={() => onRemoveFilter(item)}>
                          {item.title} = {item.value}
                        </Chip>
                      ))}
                    </ChipGroup>
                  </StackItem>
                  <StackItem>
                    <Button
                      variant={ButtonVariant.link}
                      data-testid={'list-view-toolbar-clear-filters'}
                      onClick={(event) => onClearFilters(event as any)}
                    >
                      Clear All Filters
                    </Button>
                  </StackItem>
                </Stack>
              </>
            )}
          </ToolbarItem>
        </ToolbarGroup>
      </Toolbar>
    );
  };
