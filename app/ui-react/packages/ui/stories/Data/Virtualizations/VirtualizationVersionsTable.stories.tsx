import { KebabToggle } from '@patternfly/react-core';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  IVirtualizationVersionItem,
  VirtualizationVersionsTable,
} from '../../../src';

const colHeaders = ['History', 'Published Time', 'Published', ''];

const version1Actions = <KebabToggle />;
const version2Actions = <KebabToggle />;
const version3Actions = <KebabToggle />;

const version3Item: IVirtualizationVersionItem = {
  actions: version3Actions,
  publishedState: 'RUNNING',
  timePublished: '24 Feb 2019 08:19:42 GMT',
  version: 3,
};
const version2Item: IVirtualizationVersionItem = {
  actions: version2Actions,
  publishedState: 'NOTFOUND',
  timePublished: '23 Feb 2019 08:19:42 GMT',
  version: 2,
};
const version1Item: IVirtualizationVersionItem = {
  actions: version1Actions,
  publishedState: 'NOTFOUND',
  timePublished: '22 Feb 2019 08:19:42 GMT',
  version: 1,
};
const versionItems = [version3Item, version2Item, version1Item];
const emptyVersionsTitle = 'No version history';
const emptyVersionsMsg = 'There is no version history for this virtualization.';
const publishText = 'Publish';
const draftActions: JSX.Element = <div>DraftActions</div>;
const draftText = 'Draft';

storiesOf('Data/Virtualizations/VirtualizationVersionsTable', module)
  .add('Versions - draft, no version items', () => (
    <VirtualizationVersionsTable
      a11yActionMenuColumn={'Version action menu column'}
      draftActions={draftActions}
      versionItems={[]}
      i18nDraft={draftText}
      i18nEmptyVersionsTitle={emptyVersionsTitle}
      i18nEmptyVersionsMsg={emptyVersionsMsg}
      i18nPublish={publishText}
      isModified={true}
      tableHeaders={colHeaders}
    />
  ))
  .add('Versions - no draft, no version items', () => (
    <VirtualizationVersionsTable
      a11yActionMenuColumn={'Version action menu column'}
      draftActions={draftActions}
      versionItems={[]}
      i18nDraft={draftText}
      i18nEmptyVersionsTitle={emptyVersionsTitle}
      i18nEmptyVersionsMsg={emptyVersionsMsg}
      i18nPublish={publishText}
      isModified={false}
      tableHeaders={colHeaders}
    />
  ))
  .add('Versions - draft, with version items', () => (
    <VirtualizationVersionsTable
      a11yActionMenuColumn={'Version action menu column'}
      draftActions={draftActions}
      versionItems={versionItems}
      i18nDraft={draftText}
      i18nEmptyVersionsTitle={emptyVersionsTitle}
      i18nEmptyVersionsMsg={emptyVersionsMsg}
      i18nPublish={publishText}
      isModified={true}
      tableHeaders={colHeaders}
    />
  ))
  .add('Versions - no draft, with version items', () => (
    <VirtualizationVersionsTable
      a11yActionMenuColumn={'Version action menu column'}
      draftActions={draftActions}
      versionItems={versionItems}
      i18nDraft={draftText}
      i18nEmptyVersionsTitle={emptyVersionsTitle}
      i18nEmptyVersionsMsg={emptyVersionsMsg}
      i18nPublish={publishText}
      isModified={false}
      tableHeaders={colHeaders}
    />
  ));
