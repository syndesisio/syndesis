import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationDetailHistoryListViewItem } from '../../src';

const stories = storiesOf(
  'Integration/IntegrationDetailHistoryListViewItem',
  module
);

const historyItemType = 'Draft';
const i18nTextBtnEdit = '';
const i18nTextBtnPublish = 'string';
const i18nTextDraft = 'string';
const i18nTextHistoryMenuReplaceDraft = 'string';
const i18nTextHistoryMenuUnpublish = 'string';
const i18nTextLastPublished = 'string';
const i18nTextNoDescription = 'string';

stories
  .add(
    'published',
    withNotes('Verify there is no Publish button')(() => (
      <IntegrationDetailHistoryListViewItem
        i18nTextBtnEdit={text('i18nTextBtnPublish', i18nTextBtnEdit)}
      />
    ))
  )
  .add(
    'unpublished',
    withNotes('Verify there is a Draft button')(() => (
      <IntegrationDetailHistoryListViewItem
        i18nTextBtnEdit={text('i18nTextBtnPublish', i18nTextBtnEdit)}
      />
    ))
  );
