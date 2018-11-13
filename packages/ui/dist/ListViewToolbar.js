import { Filter, FormControl, Sort, Toolbar } from 'patternfly-react';
import * as React from 'react';
export class ListViewToolbar extends React.Component {
    constructor() {
        super(...arguments);
        this.renderInput = () => {
            const { currentFilterType, currentValue } = this.props;
            if (!currentFilterType) {
                return null;
            }
            if (currentFilterType.filterType === 'select') {
                return (React.createElement(Filter.ValueSelector, { filterValues: currentFilterType.filterValues, currentValue: currentValue, onFilterValueSelected: this.props.onFilterValueSelected }));
            }
            else {
                return (React.createElement(FormControl, { type: currentFilterType.filterType, value: currentValue, placeholder: currentFilterType.placeholder, onChange: this.props.onUpdateCurrentValue, onKeyPress: this.props.onValueKeyPress }));
            }
        };
    }
    render() {
        return (React.createElement(Toolbar, null,
            React.createElement(Filter, null,
                React.createElement(Filter.TypeSelector, { filterTypes: this.props.filterTypes, currentFilterType: this.props.currentFilterType.title, onFilterTypeSelected: this.props.onSelectFilterType }),
                this.renderInput()),
            React.createElement(Sort, null,
                React.createElement(Sort.TypeSelector, { sortTypes: this.props.sortTypes, currentSortType: this.props.currentSortType, onSortTypeSelected: this.props.onUpdateCurrentSortType }),
                React.createElement(Sort.DirectionSelector, { isNumeric: false, isAscending: this.props.isSortAscending, onClick: this.props.onToggleCurrentSortDirection })),
            React.createElement(Toolbar.RightContent, null, this.props.children),
            React.createElement(Toolbar.Results, null,
                React.createElement("h5", null,
                    this.props.resultsCount,
                    " Results"),
                this.props.activeFilters &&
                    this.props.activeFilters.length > 0 && (React.createElement(React.Fragment, null,
                    React.createElement(Filter.ActiveLabel, null, "Active Filters:"),
                    React.createElement(Filter.List, null, this.props.activeFilters.map((item, index) => (React.createElement(Filter.Item, { key: index, onRemove: this.props.onRemoveFilter, filterData: item },
                        item.title,
                        "=",
                        item.value)))),
                    React.createElement("a", { onClick: this.props.onClearFilters }, "Clear All Filters"))))));
    }
}
//# sourceMappingURL=ListViewToolbar.js.map