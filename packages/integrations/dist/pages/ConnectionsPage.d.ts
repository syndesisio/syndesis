/// <reference types="react" />
import { IActiveFilter, IFilterType } from "@syndesis/ui/src";
import { IListViewToolbarAbstractComponent, ListViewToolbarAbstractComponent } from "@syndesis/utils/src";
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
