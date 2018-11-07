/// <reference types="react" />
import { IActiveFilter, IFilterType } from "@syndesis/ui";
import { IListViewToolbarAbstractComponent, ListViewToolbarAbstractComponent } from "@syndesis/utils";
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
