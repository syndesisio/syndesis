import { IActiveFilter, IFilterType } from "@syndesis/ui";
import * as React from 'react';
export interface IListViewToolbarAbstractComponent {
    activeFilters: IActiveFilter[];
    currentFilterType: IFilterType;
    currentSortType: string;
    currentValue: any;
    filterCategory: any;
    isSortAscending: boolean;
}
export declare abstract class ListViewToolbarAbstractComponent<P, S extends IListViewToolbarAbstractComponent> extends React.Component<P, S> {
    onUpdateCurrentValue: (event: Event) => void;
    onValueKeyPress: (keyEvent: KeyboardEvent) => void;
    onFilterAdded: (title: string, value: string) => void;
    onSelectFilterType: (filterType: IFilterType) => void;
    onFilterValueSelected: (filterValue: {
        id: string;
        title: string;
    }) => void;
    onRemoveFilter: (filter: IActiveFilter) => void;
    onClearFilters: (event: React.MouseEvent<HTMLAnchorElement>) => void;
    onToggleCurrentSortDirection: () => void;
    onUpdateCurrentSortType: (sortType: string) => void;
}
