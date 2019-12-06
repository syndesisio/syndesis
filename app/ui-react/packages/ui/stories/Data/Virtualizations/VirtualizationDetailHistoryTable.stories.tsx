import { KebabToggle } from '@patternfly/react-core';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  IVirtualizationHistoryItem,
  VirtualizationDetailHistoryTable,
} from '../../../src';

const colHeaders = ['History', 'Published Time', 'Published', ''];

const version1Actions = <KebabToggle />;
const version2Actions = <KebabToggle />;
const version3Actions = <KebabToggle />;

const version3Item: IVirtualizationHistoryItem = {
  actions: version3Actions,
  publishedState: 'RUNNING',
  timePublished: '24 Feb 2019 08:19:42 GMT',
  version: 3,
};
const version2Item: IVirtualizationHistoryItem = {
  actions: version2Actions,
  publishedState: 'NOTFOUND',
  timePublished: '23 Feb 2019 08:19:42 GMT',
  version: 2,
};
const version1Item: IVirtualizationHistoryItem = {
  actions: version1Actions,
  publishedState: 'NOTFOUND',
  timePublished: '22 Feb 2019 08:19:42 GMT',
  version: 1,
};
const versionItems = [version3Item, version2Item, version1Item];
const emptyVersionsTitle = 'No version history';
const emptyVersionsMsg = 'There is no version history for this virtualization.';

storiesOf('Data/Virtualizations/VirtualizationDetailHistoryTable', module)
  .add('Details', () => (
    <VirtualizationDetailHistoryTable
      a11yActionMenuColumn={'Version action menu column'}
      historyItems={versionItems}
      i18nEmptyVersionsTitle={emptyVersionsTitle}
      i18nEmptyVersionsMsg={emptyVersionsMsg}
      isModified={boolean('isModified', true)}
      tableHeaders={colHeaders}
    />
  ))
  .add('Details - no history items', () => (
    <VirtualizationDetailHistoryTable
      a11yActionMenuColumn={'Version action menu column'}
      historyItems={[]}
      i18nEmptyVersionsTitle={emptyVersionsTitle}
      i18nEmptyVersionsMsg={emptyVersionsMsg}
      isModified={boolean('isModified', true)}
      tableHeaders={colHeaders}
    />
  ));
