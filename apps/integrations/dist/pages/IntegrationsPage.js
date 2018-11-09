import { WithConnections, WithMonitoredIntegrations } from "@syndesis/api";
import { ListViewToolbarAbstractComponent } from "@syndesis/ui";
import * as React from 'react';
import { IntegrationsListView } from "../components/IntegrationsListView";
function getFilteredAndSortedIntegrations(integrations, activeFilters, currentSortType, isSortAscending) {
    let filteredAndSortedIntegrations = integrations;
    activeFilters.forEach((filter) => {
        const valueToLower = filter.value.toLowerCase();
        filteredAndSortedIntegrations = filteredAndSortedIntegrations.filter((mi) => {
            if (filter.title === 'Name') {
                return mi.integration.name.toLowerCase().includes(valueToLower);
            }
            if (filter.title === 'Connection') {
                const connectionNames = mi.integration.flows.reduce((acc, flow) => [
                    ...acc,
                    ...flow.steps
                        .filter(s => s.connection)
                        .map(s => s.connection.name.toLowerCase())
                ], []);
                return connectionNames.reduce((found, n) => found || n.includes(valueToLower), false);
            }
            return false;
        });
    });
    filteredAndSortedIntegrations = filteredAndSortedIntegrations.sort((miA, miB) => {
        const left = isSortAscending ? miA : miB;
        const right = isSortAscending ? miB : miA;
        if (currentSortType === 'Name') {
            return left.integration.name.localeCompare(right.integration.name);
        }
        return left.integration.currentState.localeCompare(right.integration.currentState);
    });
    return filteredAndSortedIntegrations;
}
const filterByName = {
    filterType: 'text',
    id: 'name',
    placeholder: 'Filter by Name',
    title: 'Name'
};
const filterByConnection = {
    filterType: 'select',
    filterValues: [],
    id: 'connection',
    placeholder: 'Filter by Connection',
    title: 'Connection'
};
function getFilterTypes(connections) {
    return [
        filterByName,
        {
            ...filterByConnection,
            filterValues: connections.map(c => ({
                id: c.id,
                title: c.name
            }))
        }
    ];
}
const sortByName = {
    id: 'name',
    isNumeric: false,
    title: 'Name'
};
const sortByStatus = {
    id: 'status',
    isNumeric: false,
    title: 'Status'
};
const sortTypes = [sortByName, sortByStatus];
export default class IntegrationsPage extends ListViewToolbarAbstractComponent {
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
        return (React.createElement(WithMonitoredIntegrations, null, ({ data: integrationsData, loading, hasData }) => (React.createElement(WithConnections, null, ({ data: connectionsData }) => {
            const filteredAndSortedIntegrations = getFilteredAndSortedIntegrations(integrationsData.items, this.state.activeFilters, this.state.currentSortType, this.state.isSortAscending);
            return (React.createElement(IntegrationsListView, Object.assign({ loading: !hasData && loading, match: 'TODO', monitoredIntegrations: filteredAndSortedIntegrations, filterTypes: getFilterTypes(connectionsData.items), sortTypes: sortTypes, resultsCount: filteredAndSortedIntegrations.length }, this.state, { onUpdateCurrentValue: this.onUpdateCurrentValue, onValueKeyPress: this.onValueKeyPress, onFilterAdded: this.onFilterAdded, onSelectFilterType: this.onSelectFilterType, onFilterValueSelected: this.onFilterValueSelected, onRemoveFilter: this.onRemoveFilter, onClearFilters: this.onClearFilters, onToggleCurrentSortDirection: this.onToggleCurrentSortDirection, onUpdateCurrentSortType: this.onUpdateCurrentSortType })));
        }))));
    }
}
//# sourceMappingURL=IntegrationsPage.js.map