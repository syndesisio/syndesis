import { Filter, FormControl, Sort, Toolbar } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { IIntegrationsMetrics, IMonitoredIntegration } from '../containers';
import { IntegrationsList } from './IntegrationsList';

export interface IIntegrationsListViewProps {
  match: any;
  monitoredIntegrations: IMonitoredIntegration[];
  integrationsCount: number;
  metrics: IIntegrationsMetrics;
}

export interface IActiveFilter {
  title: string;
  value: string;
}

export interface IFilterType {
  id: string;
  filterType: string;
  placeholder: string;
  title: string;
}

export interface IIntegrationsListViewState {
  activeFilters: IActiveFilter[];
  currentFilterType: IFilterType;
  currentSortType: string;
  currentValue: string;
  filterCategory: any;
  isSortAscending: boolean;
}

export class IntegrationsListView extends React.Component<IIntegrationsListViewProps, IIntegrationsListViewState> {
  public state = {
    activeFilters: [] as IActiveFilter[],
    currentFilterType: {
      filterType: 'text',
      id: 'name',
      placeholder: 'Filter by Name',
      title: 'Name',
    },
    currentSortType: 'Name',
    currentValue: '',
    filterCategory: null,
    isSortAscending: true,
  };

  public render() {
    const {monitoredIntegrations} = this.props;
    const {activeFilters, currentSortType, isSortAscending} = this.state;

    let filteredAndSortedIntegrations = monitoredIntegrations;
    activeFilters.forEach((filter: IActiveFilter) => {
      const valueToLower = filter.value.toLowerCase();
      filteredAndSortedIntegrations = filteredAndSortedIntegrations.filter(
        (mi: IMonitoredIntegration) => {
          if (filter.title === 'Name') {
            return mi.integration.name.toLowerCase().includes(valueToLower);
          }
          if (filter.title === 'Connection') {
            const connectionNames = mi.integration.flows
              .reduce((acc, flow) =>
                  [
                    ...acc,
                    ...flow.steps
                      .filter(s => s.connection)
                      .map(s => s.connection.name.toLowerCase())
                  ],
                []
              );
            return connectionNames.reduce((found, n) => found || n.includes(valueToLower), false);
          }
          return false;
        }
      );
    });

    filteredAndSortedIntegrations = filteredAndSortedIntegrations.sort(
      (miA, miB) => {
        const left = isSortAscending ? miA : miB;
        const right = isSortAscending ? miB : miA;
        if (currentSortType === 'Name') {
          return left.integration.name.localeCompare(right.integration.name);
        }
        return left.integration.currentState.localeCompare(right.integration.currentState);
      });

    return (
      <>
        <Toolbar>
          <Filter>
            <Filter.TypeSelector
              filterTypes={[{
                filterType: 'text',
                id: 'name',
                placeholder: 'Filter by Name',
                title: 'Name',
              }, {
                filterType: 'text',
                id: 'connection',
                placeholder: 'Filter by Connection',
                title: 'Connection',
              }]}
              currentFilterType={this.state.currentFilterType.title}
              onFilterTypeSelected={this.selectFilterType}
            />
            {this.renderInput()}
          </Filter>
          <Sort>
            <Sort.TypeSelector
              sortTypes={[{
                id: 'name',
                isNumeric: false,
                title: 'Name',
              }, {
                id: 'status',
                isNumeric: false,
                title: 'Status',
              }]}
              currentSortType={this.state.currentSortType}
              onSortTypeSelected={this.updateCurrentSortType}
            />
            <Sort.DirectionSelector
              isNumeric={false}
              isAscending={this.state.isSortAscending}
              onClick={this.toggleCurrentSortDirection}
            />
          </Sort>
          <Toolbar.RightContent>
            <div className="form-group">
              <Link
                to={`${this.props.match.url}/import`}
                className={'btn btn-default'}
              >
                Import
              </Link>
              <Link
                to={`${this.props.match.url}/new`}
                className={'btn btn-primary'}
              >
                Create Integration
              </Link>
            </div>
          </Toolbar.RightContent>
          <Toolbar.Results>
            <h5>{filteredAndSortedIntegrations.length} Results</h5>
            {this.state.activeFilters &&
            this.state.activeFilters.length > 0 && (
              <>
                <Filter.ActiveLabel>Active Filters:</Filter.ActiveLabel>
                <Filter.List>
                  {this.state.activeFilters.map((item: IActiveFilter, index) => (
                    <Filter.Item key={index} onRemove={this.removeFilter} filterData={item}>
                      {item.title}={item.value}
                    </Filter.Item>
                  ))}
                </Filter.List>
                <a onClick={this.clearFilters}>
                  Clear All Filters
                </a>
              </>
            )}
          </Toolbar.Results>
        </Toolbar>
        <div className={'container-fluid'}>
          <IntegrationsList monitoredIntegrations={filteredAndSortedIntegrations}/>
        </div>
      </>
    );
  }

  public renderInput = () => {
    const {currentFilterType, currentValue} = this.state;
    if (!currentFilterType) {
      return null;
    }
    return (
      <FormControl
        type={currentFilterType.filterType}
        value={currentValue}
        placeholder={currentFilterType.placeholder}
        onChange={this.updateCurrentValue}
        onKeyPress={this.onValueKeyPress}
      />
    );
  };

  public updateCurrentValue = (event: Event) => {
    this.setState({currentValue: (event.target as HTMLInputElement).value});
  }


  public onValueKeyPress = (keyEvent: KeyboardEvent) => {
    const {currentValue, currentFilterType} = this.state;

    if (keyEvent.key === 'Enter' && currentValue && currentValue.length > 0) {
      this.setState({currentValue: ''});
      this.filterAdded(currentFilterType.title, currentValue);
      keyEvent.stopPropagation();
      keyEvent.preventDefault();
    }
  };

  public filterAdded = (title: string, value: string) => {
    const {activeFilters} = this.state;
    this.setState({
      activeFilters: [...activeFilters, {
        title,
        value
      } as IActiveFilter]
    })
  };

  public selectFilterType = (filterType: IFilterType) => {
    const {currentFilterType} = this.state;
    if (currentFilterType !== filterType) {
      this.setState({currentValue: '', currentFilterType: filterType});
    }
  };

  public removeFilter = (filter: IActiveFilter) => {
    const {activeFilters} = this.state;

    const index = activeFilters.indexOf(filter);
    if (index > -1) {
      const updated = [
        ...activeFilters.slice(0, index),
        ...activeFilters.slice(index + 1)
      ];
      this.setState({activeFilters: updated});
    }
  };

  public clearFilters = (event: React.MouseEvent<HTMLAnchorElement>) => {
    event.preventDefault();
    this.setState({activeFilters: []});
  };

  public toggleCurrentSortDirection = () => {
    const {isSortAscending} = this.state;

    this.setState({isSortAscending: !isSortAscending});
  };

  public updateCurrentSortType = (sortType: string) => {
    const {currentSortType} = this.state;

    if (currentSortType !== sortType) {
      this.setState({
        currentSortType: sortType,
        isSortAscending: true
      });
    }
  };

}