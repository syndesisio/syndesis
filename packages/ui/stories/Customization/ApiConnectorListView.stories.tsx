import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { linkTo } from '@storybook/addon-links';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { ApiConnectorListItem, ApiConnectorListView } from '../../src';
import { extensionDetailStory } from './ExtensionDetail.stories';

const stories = storiesOf('Customization/ApiConnectorListView', module);

const connectors = [
  <ApiConnectorListItem
    apiConnectorId={text('apiConnectorId', 'api-conn-1')}
    apiConnectorDescription={text(
      'apiConnectorDescription',
      'api-conn-1-description'
    )}
    apiConnectorName={text('apiConnectorName', 'Api Conn 1')}
    i18nDelete={text('i18nDelete', 'Delete')}
    i18nDetails={text('i18nDetails', 'Details')}
    i18nUsedByMessage={text(
      'i18nUsedByMessage',
      'Not used by any integrations'
    )}
    onDelete={action('api-conn-1')}
    onDetails={linkTo('Customization', extensionDetailStory)}
    usedBy={0}
  />,
  <ApiConnectorListItem
    apiConnectorId={text('apiConnectorId', 'api-conn-2')}
    apiConnectorDescription={text(
      'apiConnectorDescription',
      'api-conn-2-description'
    )}
    apiConnectorName={text('apiConnectorName', 'Api Conn 2')}
    i18nDelete={text('i18nDescription', 'Delete')}
    i18nDetails={text('i18nDetails', 'Details')}
    i18nUsedByMessage={text('i18nUsedByMessage', 'Used by 2 integrations')}
    onDelete={action('api-conn-1')}
    onDetails={linkTo('Customization', extensionDetailStory)}
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
    withNotes(noApiConnectorsTestNotes)(() => (
      <Router>
        <ApiConnectorListView
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
          i18nLinkCreateApiConnectorTip={createConnectorTip}
          i18nName={text('i18nName', 'Name')}
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
        <ApiConnectorListView
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
