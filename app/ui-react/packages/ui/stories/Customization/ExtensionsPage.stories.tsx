import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  Container,
  ExtensionListItem,
  ExtensionListView,
  TabBar,
  TabBarItem,
} from '../../src';

storiesOf('Customization/Extensions/ExtensionsPage', module)
  .add('default scenario: with some extension to show', () => (
    <Router>
      <>
        <Container
          style={{
            background: '#fff',
          }}
        >
          <TabBar>
            <TabBarItem
              label={'apiConnectorsPageTitle'}
              to={'#api-connector'}
            />
            <TabBarItem label={'extensionsPageTitle'} to={'#extension'} />
          </TabBar>
        </Container>
        <ExtensionListView
          filterTypes={[]}
          sortTypes={[]}
          linkImportExtension={'#link-import'}
          activeFilters={[]}
          currentFilterType={{
            filterType: 'text',
            id: 'id',
            placeholder: '',
            title: '',
          }}
          currentSortType={{
            id: 'sort',
            isNumeric: false,
            title: 'Sort',
          }}
          currentValue={''}
          isSortAscending={true}
          resultsCount={1}
          onUpdateCurrentValue={action('onUpdateCurrentValue')}
          onValueKeyPress={action('onValueKeyPress')}
          onFilterAdded={action('onFilterAdded')}
          onSelectFilterType={action('onSelectFilterType')}
          onFilterValueSelected={action('onFilterValueSelected')}
          onRemoveFilter={action('onRemoveFilter')}
          onClearFilters={action('onClearFilters')}
          onToggleCurrentSortDirection={action('onToggleCurrentSortDirection')}
          onUpdateCurrentSortType={action('onUpdateCurrentSortType')}
          i18nDescription={'extensionsPageDescription'}
          i18nEmptyStateInfo={'emptyStateInfoMessage'}
          i18nEmptyStateTitle={'emptyStateTitle'}
          i18nLinkImportExtension={'ImportExtension'}
          i18nLinkImportExtensionTip={'importExtensionTip'}
          i18nName={'Name'}
          i18nNameFilterPlaceholder={'nameFilterPlaceholder'}
          i18nResultsCount={'resultsCount'}
          i18nTitle={'extensionsPageTitle'}
        >
          <ExtensionListItem
            detailsPageLink={'#ext-1-details'}
            extensionDescription={'lorem'}
            extensionIcon={<div />}
            extensionId={'abc-123'}
            extensionName={'Some extension name'}
            i18nCancelText={'Cancel'}
            i18nDelete={'Delete'}
            i18nDeleteModalMessage={'deleteModalMessage'}
            i18nDeleteModalTitle={'deleteModalTitle'}
            i18nDeleteTip={'deleteExtensionTip'}
            i18nDetails={'Details'}
            i18nDetailsTip={'detailsExtensionTip'}
            i18nExtensionType={'usedByOne'}
            i18nUpdate={'Update'}
            i18nUpdateTip={'updateExtensionTip'}
            i18nUsedByMessage={'StepExtension'}
            linkUpdateExtension={'uid'}
            onDelete={action('onDelete')}
            usedBy={0}
          />
        </ExtensionListView>
      </>
    </Router>
  ))
  .add('empty scenario: no extensions to show', () => (
    <Router>
      <>
        <Container
          style={{
            background: '#fff',
          }}
        >
          <TabBar>
            <TabBarItem
              label={'apiConnectorsPageTitle'}
              to={'#api-connector'}
            />
            <TabBarItem label={'extensionsPageTitle'} to={'#extension'} />
          </TabBar>
        </Container>
        <ExtensionListView
          filterTypes={[]}
          sortTypes={[]}
          linkImportExtension={'#link-import'}
          activeFilters={[]}
          currentFilterType={{
            filterType: 'text',
            id: 'id',
            placeholder: '',
            title: '',
          }}
          currentSortType={{
            id: 'sort',
            isNumeric: false,
            title: 'Sort',
          }}
          currentValue={''}
          isSortAscending={true}
          resultsCount={1}
          onUpdateCurrentValue={action('onUpdateCurrentValue')}
          onValueKeyPress={action('onValueKeyPress')}
          onFilterAdded={action('onFilterAdded')}
          onSelectFilterType={action('onSelectFilterType')}
          onFilterValueSelected={action('onFilterValueSelected')}
          onRemoveFilter={action('onRemoveFilter')}
          onClearFilters={action('onClearFilters')}
          onToggleCurrentSortDirection={action('onToggleCurrentSortDirection')}
          onUpdateCurrentSortType={action('onUpdateCurrentSortType')}
          i18nDescription={'extensionsPageDescription'}
          i18nEmptyStateInfo={'emptyStateInfoMessage'}
          i18nEmptyStateTitle={'emptyStateTitle'}
          i18nLinkImportExtension={'ImportExtension'}
          i18nLinkImportExtensionTip={'importExtensionTip'}
          i18nName={'Name'}
          i18nNameFilterPlaceholder={'nameFilterPlaceholder'}
          i18nResultsCount={'resultsCount'}
          i18nTitle={'extensionsPageTitle'}
        />
      </>
    </Router>
  ));
