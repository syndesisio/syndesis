import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationDetailHistoryListView } from '../../src';

const stories = storiesOf(
  'Integration/IntegrationDetailHistoryListView',
  module
);

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

const i18nTextBtnEdit = 'Edit';
const i18nTextBtnPublish = 'Publish';
const i18nTextDraft = 'Draft';
const i18nTextHistory = 'History';

stories
  .add(
    'published',
    withNotes('Verify there is no Publish button')(() => (
      <IntegrationDetailHistoryListView
        integrationIsDraft={integrationPublished.isDraft}
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
