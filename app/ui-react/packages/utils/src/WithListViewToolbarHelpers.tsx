import { IActiveFilter, IFilterType, ISortType } from '@syndesis/ui';
import * as React from 'react';

export interface IWithListViewToolbarHelpers
  extends IWithListViewToolbarHelpersState {
  onClearFilters(event: React.MouseEvent<HTMLAnchorElement>): void;
  onFilterAdded(id: string, title: string, value: string): void;
  onFilterValueSelected(filterValue: { id: string; title: string }): void;
  onRemoveFilter(filter: IActiveFilter): void;
  onSelectFilterType(filterType: IFilterType): void;
  onToggleCurrentSortDirection(): void;
  onUpdateCurrentSortType(sortType: ISortType): void;
  onUpdateCurrentValue(event: Event): void;
  onValueKeyPress(keyEvent: KeyboardEvent): void;
}

export interface IWithListViewToolbarHelpersProps {
  defaultFilterType: IFilterType;
  defaultSortType: ISortType;
  defaultSortAscending?: boolean;
  children(props: IWithListViewToolbarHelpers): any;
}

export interface IWithListViewToolbarHelpersState {
  activeFilters: IActiveFilter[];
  currentFilterType: IFilterType;
  currentSortType: ISortType;
  currentValue: any;
  filterCategory: any;
  isSortAscending: boolean;
}

export class WithListViewToolbarHelpers extends React.Component<
  IWithListViewToolbarHelpersProps,
  IWithListViewToolbarHelpersState
> {
  public static defaultProps = {
    defaultSortAscending: true,
  };

  constructor(props: IWithListViewToolbarHelpersProps) {
    super(props);
    this.state = {
      activeFilters: [] as IActiveFilter[],
      currentFilterType: this.props.defaultFilterType,
      currentSortType: this.props.defaultSortType,
      currentValue: '',
      filterCategory: null,
      isSortAscending: this.props.defaultSortAscending!,
    };
  }

  public onUpdateCurrentValue = (event: Event) => {
    this.setState({ currentValue: (event.target as HTMLInputElement).value });
  };

  public onValueKeyPress = (keyEvent: KeyboardEvent) => {
    const { currentValue, currentFilterType } = this.state;

    if (keyEvent.key === 'Enter' && currentValue && currentValue.length > 0) {
      this.setState({ currentValue: '' });
      this.onFilterAdded(
        currentFilterType.id,
        currentFilterType.title,
        currentValue
      );
      keyEvent.stopPropagation();
      keyEvent.preventDefault();
    }
  };

  public onFilterAdded = (id: string, title: string, value: string) => {
    const { activeFilters } = this.state;
    this.setState({
      activeFilters: [
        ...activeFilters,
        {
          id,
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
      this.onFilterAdded(
        currentFilterType.id,
        currentFilterType.title,
        filterValue.title
      );
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

  public onUpdateCurrentSortType = (sortType: ISortType) => {
    const { currentSortType } = this.state;

    if (currentSortType.id !== sortType.id) {
      this.setState({
        currentSortType: sortType,
        isSortAscending: true,
      });
    }
  };

  public render() {
    return this.props.children({
      onClearFilters: this.onClearFilters,
      onFilterAdded: this.onFilterAdded,
      onFilterValueSelected: this.onFilterValueSelected,
      onRemoveFilter: this.onRemoveFilter,
      onSelectFilterType: this.onSelectFilterType,
      onToggleCurrentSortDirection: this.onToggleCurrentSortDirection,
      onUpdateCurrentSortType: this.onUpdateCurrentSortType,
      onUpdateCurrentValue: this.onUpdateCurrentValue,
      onValueKeyPress: this.onValueKeyPress,
      ...this.state,
    });
  }
}
