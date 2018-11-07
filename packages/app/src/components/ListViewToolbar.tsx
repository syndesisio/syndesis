import { Filter, FormControl, Sort, Toolbar } from 'patternfly-react';
import * as React from 'react';

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
  title: string;
  value: string;
}

export interface IListViewToolbarProps {
  activeFilters: IActiveFilter[];
  filterTypes: IFilterType[];
  currentFilterType: IFilterType;
  currentSortType: string;
  currentValue: any;
  filterCategory: any;
  isSortAscending: boolean;
  resultsCount: number;
  sortTypes: ISortType[];

  onUpdateCurrentValue(event: Event): void;

  onValueKeyPress(keyEvent: KeyboardEvent): void;

  onFilterAdded(title: string, value: string): void;

  onSelectFilterType(filterType: IFilterType): void;

  onFilterValueSelected(filterValue: { id: string; title: string }): void;

  onRemoveFilter(filter: IActiveFilter): void;

  onClearFilters(event: React.MouseEvent<HTMLAnchorElement>): void;

  onToggleCurrentSortDirection(): void;

  onUpdateCurrentSortType(sortType: string): void;
}

export class ListViewToolbar extends React.Component<IListViewToolbarProps> {
  public render() {
    return (
      <Toolbar>
        <Filter>
          <Filter.TypeSelector
            filterTypes={this.props.filterTypes}
            currentFilterType={this.props.currentFilterType.title}
            onFilterTypeSelected={this.props.onSelectFilterType}
          />
          {this.renderInput()}
        </Filter>
        <Sort>
          <Sort.TypeSelector
            sortTypes={this.props.sortTypes}
            currentSortType={this.props.currentSortType}
            onSortTypeSelected={this.props.onUpdateCurrentSortType}
          />
          <Sort.DirectionSelector
            isNumeric={false}
            isAscending={this.props.isSortAscending}
            onClick={this.props.onToggleCurrentSortDirection}
          />
        </Sort>
        <Toolbar.RightContent>{this.props.children}</Toolbar.RightContent>
        <Toolbar.Results>
          <h5>{this.props.resultsCount} Results</h5>
          {this.props.activeFilters &&
            this.props.activeFilters.length > 0 && (
              <>
                <Filter.ActiveLabel>Active Filters:</Filter.ActiveLabel>
                <Filter.List>
                  {this.props.activeFilters.map(
                    (item: IActiveFilter, index) => (
                      <Filter.Item
                        key={index}
                        onRemove={this.props.onRemoveFilter}
                        filterData={item}
                      >
                        {item.title}={item.value}
                      </Filter.Item>
                    )
                  )}
                </Filter.List>
                <a onClick={this.props.onClearFilters}>Clear All Filters</a>
              </>
            )}
        </Toolbar.Results>
      </Toolbar>
    );
  }

  public renderInput = () => {
    const { currentFilterType, currentValue } = this.props;
    if (!currentFilterType) {
      return null;
    }
    if (currentFilterType.filterType === 'select') {
      return (
        <Filter.ValueSelector
          filterValues={currentFilterType.filterValues}
          currentValue={currentValue}
          onFilterValueSelected={this.props.onFilterValueSelected}
        />
      );
    } else {
      return (
        <FormControl
          type={currentFilterType.filterType}
          value={currentValue}
          placeholder={currentFilterType.placeholder}
          onChange={this.props.onUpdateCurrentValue}
          onKeyPress={this.props.onValueKeyPress}
        />
      );
    }
  };
}
