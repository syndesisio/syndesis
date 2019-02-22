import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationDetailHistoryListViewItem } from '../../src';

const stories = storiesOf(
  'Integration/IntegrationDetailHistoryListViewItem',
  module
);

const integrationPublished = {
  id: 'i-LYmlhVFB6pKKaBQVSyez',
  version: 1,
  createdAt: 1550261346722,
  updatedAt: 1550261344272,
  currentState: 'Unpublished',
  targetState: 'Unpublished',
  name: 'aaa',
  isDraft: false,
};

const integrationUnpublished = {
  id: 'i-LYmlhVFB6pKKaBQVSyez',
  version: 1,
  createdAt: 1550261346722,
  updatedAt: 1550261344272,
  currentState: 'Unpublished',
  targetState: 'Unpublished',
  name: 'aaa',
  isDraft: true,
};

const i18nTextBtnEdit = 'Edit';
const i18nTextBtnPublish = 'Publish';
const i18nTextDraft = 'Draft';
const i18nTextHistoryMenuReplaceDraft = 'Replace Draft';
const i18nTextHistoryMenuUnpublish = 'Unpublish';
const i18nTextLastPublished = 'Last published on ';
const i18nTextTitle = 'Integration Detail';
const i18nTextVersion = 'Version';

stories
  .add(
    'published',
    withNotes('Verify there is no Publish button')(() => (
      <IntegrationDetailHistoryListViewItem
        integrationUpdatedAt={text(
          'integrationUpdatedAt',
          integrationPublished.updatedAt
        )}
        integrationVersion={text(
          'integrationVersion',
          integrationPublished.version
        )}
        i18nTextBtnEdit={text('i18nTextBtnEdit', i18nTextBtnEdit)}
        i18nTextBtnPublish={text('i18nTextBtnPublish', i18nTextBtnPublish)}
        i18nTextDraft={text('i18nTextDraft', i18nTextDraft)}
        i18nTextHistoryMenuReplaceDraft={text(
          'i18nTextHistoryMenuReplaceDraft',
          i18nTextHistoryMenuReplaceDraft
        )}
        i18nTextHistoryMenuUnpublish={text(
          'i18nTextHistoryMenuUnpublish',
          i18nTextHistoryMenuUnpublish
        )}
        i18nTextLastPublished={text(
          'i18nTextLastPublished',
          i18nTextLastPublished
        )}
        i18nTextTitle={text('i18nTextTitle', i18nTextTitle)}
        i18nTextVersion={text('i18nTextVersion', i18nTextVersion)}
      />
    ))
  )
  .add(
    'unpublished',
    withNotes('Verify there is a Draft button')(() => (
      <IntegrationDetailHistoryListViewItem
        integrationVersion={text(
          'integrationVersion',
          integrationUnpublished.version
        )}
        i18nTextBtnEdit={text('i18nTextBtnEdit', i18nTextBtnEdit)}
        i18nTextBtnPublish={text('i18nTextBtnPublish', i18nTextBtnPublish)}
        i18nTextDraft={text('i18nTextDraft', i18nTextDraft)}
        i18nTextHistoryMenuReplaceDraft={text(
          'i18nTextHistoryMenuReplaceDraft',
          i18nTextHistoryMenuReplaceDraft
        )}
        i18nTextHistoryMenuUnpublish={text(
          'i18nTextHistoryMenuUnpublish',
          i18nTextHistoryMenuUnpublish
        )}
        i18nTextLastPublished={text(
          'i18nTextLastPublished',
          i18nTextLastPublished
        )}
        i18nTextTitle={text('i18nTextTitle', i18nTextTitle)}
        i18nTextVersion={text('i18nTextVersion', i18nTextVersion)}
      />
    ))
  );
