import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { DvConnectionsListView } from '../../../src';

const stories = storiesOf('Data/DvConnection/DvConnectionsListView', module);

const createConnection = 'Create Connection';
const emptyStateTitle = 'No Active Connections';
const emptyStateMsg =
  'There are no active connections available. Click Create Connection for new connection.';

stories.add('no active connections', () => (
  <Router>
    <DvConnectionsListView
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
      linkToConnectionCreate={''}
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
      i18nEmptyStateInfo={text('i18nEmptyStateInfo', emptyStateMsg)}
      i18nEmptyStateTitle={text('i18nEmptyStateTitle', emptyStateTitle)}
      i18nLinkCreateConnection={text(
        'i18nLinkCreateConnection',
        createConnection
      )}
      i18nResultsCount={text('i18nResultsCount', '0 Results')}
      children={[]}
    />
  </Router>
));
