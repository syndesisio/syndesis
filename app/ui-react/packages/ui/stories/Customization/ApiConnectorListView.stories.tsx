import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { ApiConnectorListItem, ApiConnectorListView } from '../../src';

const stories = storiesOf(
  'Customization/ApiClientConnector/ApiConnectorListView',
  module
);

const connectors = [
  <ApiConnectorListItem
    key={1}
    apiConnectorId={text('apiConnectorId', 'api-conn-1')}
    apiConnectorDescription={text(
      'apiConnectorDescription',
      'api-conn-1-description'
    )}
    apiConnectorName={text('apiConnectorName', 'Api Conn 1')}
    detailsPageLink={'/details/page/link'}
    i18nCancelLabel={'Cancel'}
    i18nDelete={'Delete'}
    i18nDeleteModalMessage={'Are you sure you want to delete this?'}
    i18nDeleteModalTitle={'Confirm Delete?'}
    i18nDetails={'Details'}
    i18nUsedByMessage={text(
      'i18nUsedByMessage',
      'Not used by any integrations'
    )}
    onDelete={action('api-conn-1')}
    usedBy={0}
  />,
  <ApiConnectorListItem
    key={2}
    apiConnectorId={text('apiConnectorId', 'api-conn-2')}
    apiConnectorDescription={text(
      'apiConnectorDescription',
      'api-conn-2-description'
    )}
    apiConnectorName={text('apiConnectorName', 'Api Conn 2')}
    detailsPageLink={'/details/page/link'}
    i18nCancelLabel={'Cancel'}
    i18nDelete={'Delete'}
    i18nDeleteModalMessage={'Are you sure you want to delete this?'}
    i18nDeleteModalTitle={'Confirm Delete?'}
    i18nDetails={'Details'}
    i18nUsedByMessage={text('i18nUsedByMessage', 'Used by 2 integrations')}
    onDelete={action('api-conn-1')}
    usedBy={2}
  />,
];

const title = 'API Client Connectors';
const description =
  'Syndesis creates an API client connector when you upload a valid OpenAPI 2.0 specification that describes the API you want to connect to.';
const createConnector = 'Create API Connector';
const createConnectorTip = 'Upload file or use URL to create API connector';

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
  '- Verify toolbar "' +
  createConnector +
  '" button tooltip is "' +
  createConnector +
  '"\n' +
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
  '- Verify toolbar "' +
  createConnector +
  '" button tooltip is "' +
  createConnectorTip +
  '"\n' +
  '- Verify results message shows 0 results\n' +
  '- Verify empty state component is displayed and has a create connector button\n' +
  '- Verify no connector list items are displayed';

stories

  .add(
    'no connectors',
    () => (
      <Router>
        <ApiConnectorListView
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
          i18nLinkCreateApiConnectorTip={createConnectorTip}
          i18nName={text('i18nName', 'Name')}
          i18nResultsCount={text('i18nResultsCount', '0 Results')}
          i18nTitle={text('i18nTitle', title)}
        />
      </Router>
    ),
    { notes: noApiConnectorsTestNotes }
  )
  .add(
    'has api connectors',
    () => (
      <Router>
        <ApiConnectorListView
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
          i18nResultsCount={text(
            'i18nResultsCount',
            connectors.length + ' Results'
          )}
          i18nTitle={text('i18nTitle', title)}
          children={connectors}
        />
      </Router>
    ),
    { notes: hasApiConnectorsTestNotes }
  );
