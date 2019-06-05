import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { render } from 'react-testing-library';
import {
  ExtensionListItem,
  ExtensionListView,
  IExtensionListViewProps,
} from '../../src/Customization';

export default describe('ExtensionListView', () => {
  const description =
    'Extensions provide custom features for use in integrations. Find out more at Syndesis Help.';
  const emptyStateInfoMessage =
    'There are no extensions available. Please click on the button below to import one.';
  const filterPlaceholder = 'Filter by name...';
  const importExtensionLink = '/extensions/import';
  const importText = 'Import Extension';
  const importTip = 'Import extension from your filesystem';
  const nameLabel = 'Name';
  const resultCountMessage = '0 Results';
  const title = 'Extensions';

  const props = {
    activeFilters: [],
    currentFilterType: {
      filterType: 'text',
      id: 'name',
      placeholder: filterPlaceholder,
      title: nameLabel,
    },
    currentSortType: {
      id: 'sort',
      isNumeric: false,
      title: 'Sort',
    },
    currentValue: '',
    filterTypes: [],
    isSortAscending: true,
    linkImportExtension: importExtensionLink,
    resultsCount: 0,
    sortTypes: [],
    onUpdateCurrentValue: jest.fn(),
    onValueKeyPress: jest.fn(),
    onFilterAdded: jest.fn(),
    onSelectFilterType: jest.fn(),
    onFilterValueSelected: jest.fn(),
    onRemoveFilter: jest.fn(),
    onClearFilters: jest.fn(),
    onToggleCurrentSortDirection: jest.fn(),
    onUpdateCurrentSortType: jest.fn(),
    i18nDescription: description,
    i18nEmptyStateInfo: emptyStateInfoMessage,
    i18nEmptyStateTitle: importText,
    i18nLinkImportExtension: importText,
    i18nLinkImportExtensionTip: importTip,
    i18nName: nameLabel,
    i18nNameFilterPlaceholder: filterPlaceholder,
    i18nResultsCount: resultCountMessage,
    i18nTitle: title,
  } as IExtensionListViewProps;

  it('Should render empty state', () => {
    const comp = (
      <Router>
        <ExtensionListView {...props} />
      </Router>
    );

    const { queryAllByPlaceholderText, queryAllByText } = render(comp);

    // filter placeholder text
    expect(queryAllByPlaceholderText(filterPlaceholder)).toHaveLength(1);

    // import button (toolbar button, empty state title, empty state button)
    const importComps = queryAllByText(importText);
    expect(importComps).toHaveLength(3);

    // make sure links are set on import buttons
    importComps.map(ic => {
      if (ic.className.includes('btn')) {
        expect(ic).toHaveAttribute('href', importExtensionLink);
      }
    });

    // title
    expect(queryAllByText(title)).toHaveLength(1);

    // description
    expect(queryAllByText(description)).toHaveLength(1);

    // empty state message
    expect(queryAllByText(emptyStateInfoMessage)).toHaveLength(1);
  });

  it('Should render extensions', () => {
    const extOneName = 'extension-one-name';
    const extTwoName = 'extension-two-name';

    const comp = (
      <Router>
        <ExtensionListView {...props}>
          <ExtensionListItem
            detailsPageLink={'/extensions/id-0'}
            extensionDescription="id-0 description goes here"
            extensionIcon={<div />}
            extensionId={'id-0'}
            extensionName={extOneName}
            i18nCancelText={'Cancel'}
            i18nDelete={'Delete'}
            i18nDeleteModalMessage={
              'Are you sure you want to delete the "id-0" extension?'
            }
            i18nDeleteModalTitle={'Confirm Delete?'}
            i18nDetails={'Details'}
            i18nExtensionType={'Step Extension'}
            i18nUpdate={'Update'}
            i18nUsedByMessage={'Used by 0 integration(s)'}
            linkUpdateExtension={'/extensions/update'}
            onDelete={jest.fn()}
            usedBy={0}
          />
          <ExtensionListItem
            detailsPageLink={'/extensions/id-1'}
            extensionDescription="id-1 description goes here"
            extensionIcon={<div />}
            extensionId={'id-1'}
            extensionName={extTwoName}
            i18nCancelText={'Cancel'}
            i18nDelete={'Delete'}
            i18nDeleteModalMessage={
              'Are you sure you want to delete the "id-1" extension?'
            }
            i18nDeleteModalTitle={'Confirm Delete?'}
            i18nDetails={'Details'}
            i18nExtensionType={'Step Extension'}
            i18nUpdate={'Update'}
            i18nUsedByMessage={'Used by 0 integration(s)'}
            linkUpdateExtension={'/extensions/update'}
            onDelete={jest.fn()}
            usedBy={0}
          />
        </ExtensionListView>
      </Router>
    );

    const { getByText, queryAllByPlaceholderText, queryAllByText } = render(
      comp
    );

    // filter placeholder text
    expect(queryAllByPlaceholderText(filterPlaceholder)).toHaveLength(1);

    // import button in toolbar
    expect(queryAllByText(importText)).toHaveLength(1);
    expect(getByText(importText)).toHaveAttribute('href', importExtensionLink);

    // title
    expect(queryAllByText(title)).toHaveLength(1);

    // description
    expect(queryAllByText(description)).toHaveLength(1);

    // make sure both list items are displayed
    expect(queryAllByText(extOneName)).toHaveLength(1);
    expect(queryAllByText(extTwoName)).toHaveLength(1);
  });
});
