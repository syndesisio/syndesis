import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import {
  ConnectionCard,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionsListView,
} from '../../src';

const stories = storiesOf('Connection/ConnectionsListView', module);

const techPreviewPopoverHtml = (
  <div>
    <a
      href="https://access.redhat.com/support/offerings/techpreview"
      rel="nofollow"
      target="_blank"
      role="link"
    >
      Technology Previews
    </a>{' '}
    provide early access to features that are not yet supported. Feedback about
    these features is welcome. Send a message to{' '}
    <a href="mailto:fuse-online-tech-preview@redhat.com">
      fuse-online-tech-preview@redhat.com
    </a>
    .
  </div>
);

const connections = (
  <ConnectionsGrid>
    <ConnectionsGridCell>
      <ConnectionCard
        name={text('name', 'Connection 1')}
        description={text('description', 'Connection 1 description')}
        href={'#example'}
        i18nTechPreview={'Technology Preview'}
        icon={<div />}
      />
    </ConnectionsGridCell>
    <ConnectionsGridCell>
      <ConnectionCard
        name={text('name', 'Connection 2')}
        description={text('description', 'Connection 2 description')}
        href={'#example'}
        icon={<div />}
      />
    </ConnectionsGridCell>
    <ConnectionsGridCell>
      <ConnectionCard
        name={text('name', 'Connection 3')}
        description={text('description', 'Config required and tech preview.')}
        href={'#example'}
        i18nConfigRequired={'Configuration Required'}
        i18nTechPreview={'Technology Preview'}
        icon={<div />}
        isConfigRequired={true}
        isTechPreview={true}
        techPreviewPopoverHtml={techPreviewPopoverHtml}
      />
    </ConnectionsGridCell>
  </ConnectionsGrid>
);

stories

  .add('empty', () => (
    <Router>
      <ConnectionsListView
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
        linkToConnectionCreate={action('/connections/create')}
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
        i18nLinkCreateConnection={text(
          'i18nLinkCreateConnection',
          'Create Connection'
        )}
        i18nResultsCount={text('i18nResultsCount', '0 Results')}
      />
    </Router>
  ))
  .add('with children', () => (
    <Router>
      <div className="container-pf-nav-pf-vertical">
        <ConnectionsListView
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
          linkToConnectionCreate={'/connections/create'}
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
          i18nLinkCreateConnection={text(
            'i18nLinkCreateConnection',
            'Create Connection'
          )}
          i18nResultsCount={text('i18nResultsCount', '2 Results')}
          children={connections}
        />
      </div>
    </Router>
  ));
