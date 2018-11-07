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
    onFilterValueSelected(filterValue: {
        id: string;
        title: string;
    }): void;
    onRemoveFilter(filter: IActiveFilter): void;
    onClearFilters(event: React.MouseEvent<HTMLAnchorElement>): void;
    onToggleCurrentSortDirection(): void;
    onUpdateCurrentSortType(sortType: string): void;
}
export declare class ListViewToolbar extends React.Component<IListViewToolbarProps> {
    render(): JSX.Element;
    renderInput: () => JSX.Element | null;
}
