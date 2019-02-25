import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import {
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
} from '../../src';

const stories = storiesOf(
  'Integration/IntegrationDetailHistoryListView',
  module
);

const i18nTextBtnEdit = 'Edit';
const i18nTextBtnPublish = 'Publish';
const i18nTextDraft = 'Draft';
const i18nTextHistory = 'History';
const i18nTextHistoryMenuReplaceDraft = 'Replace Draft';
const i18nTextHistoryMenuUnpublish = 'Unpublish';
const i18nTextLastPublished = 'Last published on ';
const i18nTextVersion = 'Version';

const integrationPublished = {
  id: 'i-LYmlhVFB6pKKaBQVSyez',
  version: 1,
  updatedAt: 1550261344272,
  currentState: 'Unpublished',
  targetState: 'Unpublished',
  name: 'aaa',
  isDraft: false,
};

const integrationUnpublished = {
  id: 'i-LYmlhVFB6pKKaBQVSyez',
  version: 1,
  updatedAt: 1550261344272,
  currentState: 'Unpublished',
  targetState: 'Unpublished',
  name: 'aaa',
  isDraft: true,
};

const integrationPublishedHistoryItems = [
  <IntegrationDetailHistoryListViewItem
    key={0}
    integrationUpdatedAt={text(
      'integrationUpdatedAt',
      integrationPublished.updatedAt
    )}
    integrationVersion={text(
      'integrationVersion',
      integrationPublished.version
    )}
    i18nTextHistoryMenuReplaceDraft={text(
      'i18nTextHistoryMenuReplaceDraft',
      i18nTextHistoryMenuReplaceDraft
    )}
    i18nTextHistoryMenuUnpublish={text(
      'i18nTextHistoryMenuUnpublish',
      i18nTextHistoryMenuUnpublish
    )}
    i18nTextLastPublished={text('i18nTextLastPublished', i18nTextLastPublished)}
    i18nTextVersion={text('i18nTextVersion', i18nTextVersion)}
  />,
];

stories
  .add(
    'published',
    withNotes('Verify there is no Publish button')(() => (
      <IntegrationDetailHistoryListView
        integrationIsDraft={integrationPublished.isDraft}
        children={integrationPublishedHistoryItems}
        i18nTextDraft={text('i18nTextDraft', i18nTextDraft)}
        i18nTextHistory={text('i18nTextHistory', i18nTextHistory)}
      />
    ))
  )
  .add(
    'draft, no published history',
    withNotes('Verify there is a Publish button')(() => (
      <IntegrationDetailHistoryListView
        integrationIsDraft={integrationUnpublished.isDraft}
        i18nTextBtnEdit={text('i18nTextBtnEdit', i18nTextBtnEdit)}
        i18nTextBtnPublish={text('i18nTextBtnPublish', i18nTextBtnPublish)}
        i18nTextDraft={text('i18nTextDraft', i18nTextDraft)}
        i18nTextHistory={text('i18nTextHistory', i18nTextHistory)}
      />
    ))
  );
