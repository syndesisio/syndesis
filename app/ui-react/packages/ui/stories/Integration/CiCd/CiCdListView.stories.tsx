import { action } from '@storybook/addon-actions';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { CiCdList, CiCdListItem, CiCdListView } from '../../../src';
import { CiCdListEmptyState } from '../../../src';

const stories = storiesOf('/Integration/CiCd/CiCdListView', module);
stories.addDecorator(withKnobs);

stories
  .add('with children', () => (
    <CiCdListView
      activeFilters={[]}
      currentFilterType={{
        filterType: 'text',
        id: 'name',
        placeholder: text('placeholder', 'Filter by name'),
        title: text('title', 'Name'),
      }}
      currentSortType={{
        id: 'sort',
        isNumeric: false,
        title: 'Sort',
      }}
      currentValue={''}
      filterTypes={[]}
      isSortAscending={true}
      resultsCount={2}
      sortTypes={[]}
      onUpdateCurrentValue={action('onUpdateCurrentValue')}
      onValueKeyPress={action('onValueKeyPress')}
      onFilterAdded={action('onFilterAdded')}
      onSelectFilterType={action('onSelectFilterType')}
      onFilterValueSelected={action('onFilterValueSelected')}
      onRemoveFilter={action('onRemoveFilter')}
      onClearFilters={action('onClearFilters')}
      onToggleCurrentSortDirection={action('onToggleCurrentSortDirection')}
      onUpdateCurrentSortType={action('onUpdateCurrentSortType')}
      onAddNew={action('onAddNew')}
      i18nAddNewButtonText={text('Add New Text', 'Add New')}
      i18nResultsCount={text('i18nResultsCount', '2 Results')}
      children={
        <CiCdList
          children={[
            {
              i18nUsesText: 'Used by 3 integrations',
              name: 'Environment 1',
            },
            {
              i18nUsesText: 'Used by 0 integrations',
              name: 'Environment 2',
            },
          ].map((env, index) => (
            <CiCdListItem
              key={index}
              onEditClicked={action('onEditClicked')}
              onRemoveClicked={action('onRemoveClicked')}
              i18nEditButtonText={text('Edit Button', 'Edit')}
              i18nRemoveButtonText={text('Remove Button', 'Remove')}
              name={env.name}
              i18nUsesText={env.i18nUsesText}
            />
          ))}
        />
      }
    />
  ))
  .add('empty state', () => (
    <CiCdListView
      activeFilters={[]}
      currentFilterType={{
        filterType: 'text',
        id: 'name',
        placeholder: text('placeholder', 'Filter by name'),
        title: text('title', 'Name'),
      }}
      currentSortType={{
        id: 'sort',
        isNumeric: false,
        title: 'Sort',
      }}
      currentValue={''}
      filterTypes={[]}
      isSortAscending={true}
      resultsCount={0}
      sortTypes={[]}
      onUpdateCurrentValue={action('onUpdateCurrentValue')}
      onValueKeyPress={action('onValueKeyPress')}
      onFilterAdded={action('onFilterAdded')}
      onSelectFilterType={action('onSelectFilterType')}
      onFilterValueSelected={action('onFilterValueSelected')}
      onRemoveFilter={action('onRemoveFilter')}
      onClearFilters={action('onClearFilters')}
      onToggleCurrentSortDirection={action('onToggleCurrentSortDirection')}
      onUpdateCurrentSortType={action('onUpdateCurrentSortType')}
      onAddNew={action('onAddNew')}
      i18nResultsCount={text('i18nResultsCount', '0 Results')}
      i18nAddNewButtonText={text('Add New Text', 'Add New')}
      children={
        <CiCdListEmptyState
          onAddNew={action('onAddNew')}
          i18nAddNewButtonText={text('Add New Text', 'Add New')}
          i18nTitle={text('Empty State Title', 'No Environments Available')}
          i18nInfo={text('Empty State Info', '')}
        />
      }
    />
  ));
