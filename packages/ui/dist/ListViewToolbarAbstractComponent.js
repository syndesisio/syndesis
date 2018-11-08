import * as React from 'react';
export class ListViewToolbarAbstractComponent extends React.Component {
    constructor() {
        super(...arguments);
        this.onUpdateCurrentValue = (event) => {
            this.setState({ currentValue: event.target.value });
        };
        this.onValueKeyPress = (keyEvent) => {
            const { currentValue, currentFilterType } = this.state;
            if (keyEvent.key === 'Enter' && currentValue && currentValue.length > 0) {
                this.setState({ currentValue: '' });
                this.onFilterAdded(currentFilterType.title, currentValue);
                keyEvent.stopPropagation();
                keyEvent.preventDefault();
            }
        };
        this.onFilterAdded = (title, value) => {
            const { activeFilters } = this.state;
            this.setState({
                activeFilters: [
                    ...activeFilters,
                    {
                        title,
                        value
                    }
                ]
            });
        };
        this.onSelectFilterType = (filterType) => {
            const { currentFilterType } = this.state;
            if (currentFilterType !== filterType) {
                this.setState({ currentValue: '', currentFilterType: filterType });
            }
        };
        this.onFilterValueSelected = (filterValue) => {
            const { currentFilterType } = this.state;
            this.setState({ currentValue: filterValue.title });
            if (filterValue) {
                this.onFilterAdded(currentFilterType.title, filterValue.title);
            }
        };
        this.onRemoveFilter = (filter) => {
            const { activeFilters } = this.state;
            const index = activeFilters.indexOf(filter);
            if (index > -1) {
                const updated = [
                    ...activeFilters.slice(0, index),
                    ...activeFilters.slice(index + 1)
                ];
                this.setState({ activeFilters: updated });
            }
        };
        this.onClearFilters = (event) => {
            event.preventDefault();
            this.setState({ activeFilters: [] });
        };
        this.onToggleCurrentSortDirection = () => {
            const { isSortAscending } = this.state;
            this.setState({ isSortAscending: !isSortAscending });
        };
        this.onUpdateCurrentSortType = (sortType) => {
            const { currentSortType } = this.state;
            if (currentSortType !== sortType) {
                this.setState({
                    currentSortType: sortType,
                    isSortAscending: true
                });
            }
        };
    }
}
//# sourceMappingURL=ListViewToolbarAbstractComponent.js.map