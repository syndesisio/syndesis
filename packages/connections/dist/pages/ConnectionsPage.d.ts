/// <reference types="react" />
import { IActiveFilter, IFilterType, IListViewToolbarAbstractComponent, ListViewToolbarAbstractComponent } from "@syndesis/ui";
export default class ConnectionsPage extends ListViewToolbarAbstractComponent<{}, IListViewToolbarAbstractComponent> {
    state: {
        activeFilters: IActiveFilter[];
        currentFilterType: IFilterType;
        currentSortType: string;
        currentValue: string;
        filterCategory: null;
        isSortAscending: boolean;
    };
    render(): JSX.Element;
}
