import * as React from 'react';
import { IActiveFilter, IFilterType } from './ListViewToolbar';

export interface IListViewToolbarAbstractComponent {
  activeFilters: IActiveFilter[];
  currentFilterType: IFilterType;
  currentSortType: string;
  currentValue: any;
  filterCategory: any;
  isSortAscending: boolean;
}

export abstract class ListViewToolbarAbstractComponent<
  P,
  S extends IListViewToolbarAbstractComponent
> extends React.Component<P, S> {
  public onUpdateCurrentValue = (event: Event) => {
    this.setState({ currentValue: (event.target as HTMLInputElement).value });
  };

  public onValueKeyPress = (keyEvent: KeyboardEvent) => {
    const { currentValue, currentFilterType } = this.state;

    if (keyEvent.key === 'Enter' && currentValue && currentValue.length > 0) {
      this.setState({ currentValue: '' });
      this.onFilterAdded(currentFilterType.title, currentValue);
      keyEvent.stopPropagation();
      keyEvent.preventDefault();
    }
  };

  public onFilterAdded = (title: string, value: string) => {
    const { activeFilters } = this.state;
    this.setState({
      activeFilters: [
        ...activeFilters,
        {
          title,
          value,
        } as IActiveFilter,
      ],
    });
  };

  public onSelectFilterType = (filterType: IFilterType) => {
    const { currentFilterType } = this.state;
    if (currentFilterType !== filterType) {
      this.setState({ currentValue: '', currentFilterType: filterType });
    }
  };

  public onFilterValueSelected = (filterValue: {
    id: string;
    title: string;
  }) => {
    const { currentFilterType } = this.state;

    this.setState({ currentValue: filterValue.title });
    if (filterValue) {
      this.onFilterAdded(currentFilterType.title, filterValue.title);
    }
  };

  public onRemoveFilter = (filter: IActiveFilter) => {
    const { activeFilters } = this.state;

    const index = activeFilters.indexOf(filter);
    if (index > -1) {
      const updated = [
        ...activeFilters.slice(0, index),
        ...activeFilters.slice(index + 1),
      ];
      this.setState({ activeFilters: updated });
    }
  };

  public onClearFilters = (event: React.MouseEvent<HTMLAnchorElement>) => {
    event.preventDefault();
    this.setState({ activeFilters: [] });
  };

  public onToggleCurrentSortDirection = () => {
    const { isSortAscending } = this.state;

    this.setState({ isSortAscending: !isSortAscending });
  };

  public onUpdateCurrentSortType = (sortType: string) => {
    const { currentSortType } = this.state;

    if (currentSortType !== sortType) {
      this.setState({
        currentSortType: sortType,
        isSortAscending: true,
      });
    }
  };
}
