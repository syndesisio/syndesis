import {
  ConnectionsListView,
  IActiveFilter,
  IConnectionsListViewProps,
  IFilterType,
  ISortType,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../../i18n';
import resolvers from '../../../connections/resolvers';
import { EditorSteps, IEditorStepsProps } from './EditorSteps';
import { IUIStep } from './interfaces';

function getFilteredAndSortedEditorSteps(
  steps: IUIStep[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean
) {
  let filteredAndSortedEditorSteps = steps;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSortedEditorSteps = filteredAndSortedEditorSteps.filter(
      (e: IUIStep) => e.name.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSortedEditorSteps = filteredAndSortedEditorSteps.sort(
    (miA, miB) => {
      const left = isSortAscending ? miA : miB;
      const right = isSortAscending ? miB : miA;
      return left.name.localeCompare(right.name);
    }
  );

  return filteredAndSortedEditorSteps;
}

const filterByName = {
  filterType: 'text',
  id: 'name',
  placeholder: i18n.t('shared:filterByNamePlaceholder'),
  title: i18n.t('shared:Name'),
} as IFilterType;

const filterTypes = [filterByName];

const sortByName = {
  id: 'name',
  isNumeric: false,
  title: i18n.t('shared:Name'),
} as ISortType;

const sortTypes: ISortType[] = [sortByName];

export interface IEditorStepsWithToolbarProps
  extends IEditorStepsProps,
    Pick<IConnectionsListViewProps, 'createConnectionButtonStyle'> {
  children?: any;
}

export class EditorStepsWithToolbar extends React.Component<
  IEditorStepsWithToolbarProps
> {
  public render() {
    return (
      <Translation ns={['connections', 'shared']}>
        {t => (
          <WithListViewToolbarHelpers
            defaultFilterType={filterByName}
            defaultSortType={sortByName}
          >
            {helpers => {
              const filteredAndSortedEditorSteps = getFilteredAndSortedEditorSteps(
                this.props.steps,
                helpers.activeFilters,
                helpers.currentSortType,
                helpers.isSortAscending
              );

              return (
                <ConnectionsListView
                  createConnectionButtonStyle={
                    this.props.createConnectionButtonStyle
                  }
                  linkToConnectionCreate={resolvers.create.selectConnector()}
                  filterTypes={filterTypes}
                  sortTypes={sortTypes}
                  resultsCount={filteredAndSortedEditorSteps.length}
                  {...helpers}
                  i18nLinkCreateConnection={t('shared:linkCreateConnection')}
                  i18nResultsCount={t('shared:resultsCount', {
                    count: filteredAndSortedEditorSteps.length,
                  })}
                >
                  {this.props.children}
                  <EditorSteps
                    error={this.props.error}
                    loading={this.props.loading}
                    steps={filteredAndSortedEditorSteps}
                    getEditorStepHref={this.props.getEditorStepHref}
                  />
                </ConnectionsListView>
              );
            }}
          </WithListViewToolbarHelpers>
        )}
      </Translation>
    );
  }
}
