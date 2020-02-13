import { IntegrationOverview } from '@syndesis/models';
import {
  IActiveFilter,
  IFilterType,
  IntegrationsList,
  IntegrationsListItemBasic,
  ISortType,
  ListViewToolbar,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers } from '@syndesis/utils';
import * as React from 'react';
import i18n from '../../../i18n';

export interface ISelectiveIntegrationListProps {
  data: any;
  isChecked(name: string): boolean;
  onIntegrationChecked(checked: boolean, name: string): void;
}

export const SelectiveIntegrationList: React.FunctionComponent<ISelectiveIntegrationListProps> = ({
  data,
  isChecked,
  onIntegrationChecked,
  children,
}) => {
  const filterByName = {
    filterType: 'text',
    id: 'name',
    placeholder: 'Filter By Name',
    title: 'Name',
  } as IFilterType;
  const filterTypes = [filterByName];
  const sortByName = {
    id: 'name',
    isNumeric: false,
    title: 'Name',
  } as ISortType;
  const sortTypes: ISortType[] = [sortByName];

  const getFilteredAndSortedIntegrations = (
    integrations: IntegrationOverview[],
    activeFilters: IActiveFilter[],
    currentSortType: ISortType,
    isSortAscending: boolean
  ) => {
    let filteredAndSortedIntegrations = integrations;

    activeFilters.forEach((filter: IActiveFilter) => {
      const valueToLower = filter.value.toLowerCase();
      filteredAndSortedIntegrations = filteredAndSortedIntegrations.filter(
        (si: IntegrationOverview) => {
          if (filter.id === 'name') {
            return si.name.toLowerCase().includes(valueToLower);
          }
          return false;
        }
      );
    });

    filteredAndSortedIntegrations = filteredAndSortedIntegrations.sort(
      (siA, siB) => {
        const left = isSortAscending ? siA : siB;
        const right = isSortAscending ? siB : siA;
        if (currentSortType.id === 'name') {
          return left.name.localeCompare(right.name);
        }
        return left.currentState!.localeCompare(right.currentState!);
      }
    );

    return filteredAndSortedIntegrations;
  };

  return (
    <div className="container-fluid pf-u-my-lg">
      <WithListViewToolbarHelpers
        defaultFilterType={filterByName}
        defaultSortType={sortByName}
      >
        {helpers => {
          const filteredAndSortedIntegrations = getFilteredAndSortedIntegrations(
            data.items,
            helpers.activeFilters,
            helpers.currentSortType,
            helpers.isSortAscending
          );
          return (
            <>
              <ListViewToolbar
                {...helpers}
                filterTypes={filterTypes}
                sortTypes={sortTypes}
                resultsCount={filteredAndSortedIntegrations.length}
                i18nResultsCount={`${filteredAndSortedIntegrations.length} Results`}
              />
              <IntegrationsList
                i18nAriaLabel={i18n.t('integrations:IntegrationsListAriaLabel')}
              >
                {filteredAndSortedIntegrations.map(
                  (si: IntegrationOverview) => {
                    return (
                      <IntegrationsListItemBasic
                        className="support-integration-list-item"
                        key={`${si.updatedAt}-${si.name.split(' ').join('-')}`}
                        additionalInfo={si.description || ''}
                        integrationName={si.name}
                        isChecked={isChecked}
                        onCheck={onIntegrationChecked}
                      />
                    );
                  }
                )}
              </IntegrationsList>
              {children}
            </>
          );
        }}
      </WithListViewToolbarHelpers>
    </div>
  );
};
