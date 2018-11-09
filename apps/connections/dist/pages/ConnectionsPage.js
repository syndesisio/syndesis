import { WithConnections } from "@syndesis/api";
import { ListViewToolbarAbstractComponent } from "@syndesis/ui";
import * as React from 'react';
import { ConnectionsAppContext } from "../ConnectionsAppContext";
import { ConnectionsListView } from "../components/ConnectionsListView";
function getFilteredAndSortedConnections(connections, activeFilters, currentSortType, isSortAscending) {
    let filteredAndSortedConnections = connections;
    activeFilters.forEach((filter) => {
        const valueToLower = filter.value.toLowerCase();
        filteredAndSortedConnections = filteredAndSortedConnections.filter((c) => c.name.toLowerCase().includes(valueToLower));
    });
    filteredAndSortedConnections = filteredAndSortedConnections.sort((miA, miB) => {
        const left = isSortAscending ? miA : miB;
        const right = isSortAscending ? miB : miA;
        return left.name.localeCompare(right.name);
    });
    return filteredAndSortedConnections;
}
const filterByName = {
    filterType: 'text',
    id: 'name',
    placeholder: 'Filter by Name',
    title: 'Name'
};
const filterTypes = [filterByName];
const sortByName = {
    id: 'name',
    isNumeric: false,
    title: 'Name'
};
const sortTypes = [sortByName];
export default class ConnectionsPage extends ListViewToolbarAbstractComponent {
    constructor() {
        super(...arguments);
        this.state = {
            activeFilters: [],
            currentFilterType: filterByName,
            currentSortType: sortByName.title,
            currentValue: '',
            filterCategory: null,
            isSortAscending: true
        };
    }
    render() {
        return (React.createElement(ConnectionsAppContext.Consumer, null, ({ baseurl }) => React.createElement(WithConnections, null, ({ data, loading, hasData }) => {
            const filteredAndSortedConnections = getFilteredAndSortedConnections(data.items, this.state.activeFilters, this.state.currentSortType, this.state.isSortAscending);
            return (React.createElement(ConnectionsListView, Object.assign({ loading: !hasData && loading, baseurl: baseurl, connections: filteredAndSortedConnections, filterTypes: filterTypes, sortTypes: sortTypes, resultsCount: filteredAndSortedConnections.length }, this.state, { onUpdateCurrentValue: this.onUpdateCurrentValue, onValueKeyPress: this.onValueKeyPress, onFilterAdded: this.onFilterAdded, onSelectFilterType: this.onSelectFilterType, onFilterValueSelected: this.onFilterValueSelected, onRemoveFilter: this.onRemoveFilter, onClearFilters: this.onClearFilters, onToggleCurrentSortDirection: this.onToggleCurrentSortDirection, onUpdateCurrentSortType: this.onUpdateCurrentSortType })));
        })));
    }
}
//# sourceMappingURL=ConnectionsPage.js.map