import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationDetailHistoryListViewItem } from '../../src';

const stories = storiesOf(
  'Integration/Detail/Components/IntegrationDetailHistoryListViewItem',
  module
);

const datePublished = Date.parse('24 Feb 2019 08:19:42 GMT');

const integrationPublished = {
  updatedAt: datePublished,
  version: 1,
};

const i18nTextHistoryMenuReplaceDraft = 'Replace Draft';
const i18nTextHistoryMenuUnpublish = 'Unpublish';
const i18nTextLastPublished = 'Last published on ';
const i18nTextVersion = 'Version';

stories.add(
  'published',
  withNotes('Verify there is a list of history items')(() => (
    <IntegrationDetailHistoryListViewItem
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
      i18nTextLastPublished={text(
        'i18nTextLastPublished',
        i18nTextLastPublished
      )}
      i18nTextVersion={text('i18nTextVersion', i18nTextVersion)}
    />
  ))
);
