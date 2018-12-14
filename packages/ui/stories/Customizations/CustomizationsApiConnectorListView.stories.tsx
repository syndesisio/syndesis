import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import {
  CustomizationsApiConnectorListItem,
  CustomizationsApiConnectorListView,
} from '../../src/Customizations';

const connectors = [
  <CustomizationsApiConnectorListItem
    apiConnectorId={text('apiConnectorId', 'api-conn-1')}
    apiConnectorDescription={text(
      'apiConnectorDescription',
      'api-conn-1-description'
    )}
    apiConnectorName={text('apiConnectorName', 'Api Conn 1')}
    i18nDelete={text('i18nDescription', 'Delete')}
    i18nUsedByMessage={text('i18nUsedByMessage', 'Used by 1 integration')}
    onDelete={action('api-conn-1')}
    usedBy={1}
  />,
  <CustomizationsApiConnectorListItem
    apiConnectorId={text('apiConnectorId', 'api-conn-2')}
    apiConnectorDescription={text(
      'apiConnectorDescription',
      'api-conn-2-description'
    )}
    apiConnectorName={text('apiConnectorName', 'Api Conn 2')}
    i18nDelete={text('i18nDescription', 'Delete')}
    i18nUsedByMessage={text('i18nUsedByMessage', 'Used by 2 integrations')}
    onDelete={action('api-conn-1')}
    usedBy={2}
  />,
];

const title = 'API Client Connectors';
const description =
  'Syndesis creates an API client connector when you upload a valid OpenAPI 2.0 specification that describes the API you want to connect to.';
const createConnector = 'Create API Connector';

const hasApiConnectorsTestNotes =
  '- Verify page title is "' +
  title +
  '"\n' +
  '- Verify page description is "' +
  description +
  '"\n' +
  '- Verify toolbar is displayed\n' +
  '- Verify toolbar contains "' +
  createConnector +
  '" button\n' +
  '- Verify toolbar "' +
  createConnector +
  '" button is enabled\n' +
  '- Verify empty state component does not show\n' +
  '- Verify results message shows ' +
  connectors.length +
  ' results\n' +
  '- Verify ' +
  connectors.length +
  ' API connectors list items are displayed';

const noApiConnectorsTestNotes =
  '- Verify page title is "' +
  title +
  '"\n' +
  '- Verify page description is "' +
  description +
  '"\n' +
  '- Verify toolbar is displayed\n' +
  '- Verify toolbar contains "' +
  createConnector +
  '" button\n' +
  '- Verify toolbar "' +
  createConnector +
  '" button is enabled\n' +
  '- Verify empty state component is displayed\n' +
  '- Verify no connector list items are displayed\n' +
  '- Verify results message shows 0 results';

const stories = storiesOf('CustomizationsApiConnectorListView', module);

stories

  .add(
    'no connectors',
    withNotes(noApiConnectorsTestNotes)(() => (
      <Router>
        <CustomizationsApiConnectorListView
          activeFilters={[]}
          currentFilterType={{
            filterType: 'text',
            id: 'name',
            placeholder: text('placeholder', 'Filter by name'),
            title: text('title', 'Name'),
          }}
          currentSortType={'sort'}
          currentValue={''}
          filterTypes={[]}
          isSortAscending={true}
          linkCreateApiConnector={action('/customizations/create')}
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
          i18nDescription={text('i18nDescription', description)}
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available API connectors. Please click on the button below to create one.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', createConnector)}
          i18nLinkCreateApiConnector={text(
            'i18nLinkCreateApiConnector',
            createConnector
          )}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text('i18nResultsCount', '0 Results')}
          i18nTitle={text('i18nTitle', title)}
        />
      </Router>
    ))
  )
  .add(
    'has api connectors',
    withNotes(hasApiConnectorsTestNotes)(() => (
      <Router>
        <CustomizationsApiConnectorListView
          activeFilters={[]}
          currentFilterType={{
            filterType: 'text',
            id: 'name',
            placeholder: text('placeholder', 'Filter by name'),
            title: text('title', 'Name'),
          }}
          currentSortType={'sort'}
          currentValue={''}
          filterTypes={[]}
          isSortAscending={true}
          linkCreateApiConnector={action('/customizations/create')}
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
          i18nDescription={text('i18nDescription', description)}
          i18nEmptyStateInfo={text(
            'i18nEmptyStateInfo',
            'There are no currently available API connectors. Please click on the button below to create one.'
          )}
          i18nEmptyStateTitle={text('i18nEmptyStateTitle', createConnector)}
          i18nLinkCreateApiConnector={text(
            'i18nLinkCreateApiConnector',
            createConnector
          )}
          i18nName={text('i18nName', 'Name')}
          i18nNameFilterPlaceholder={text(
            'i18nNameFilterPlaceholder',
            'Filter by Name...'
          )}
          i18nResultsCount={text(
            'i18nResultsCount',
            connectors.length + ' Results'
          )}
          i18nTitle={text('i18nTitle', title)}
          children={connectors}
        />
      </Router>
    ))
  );
