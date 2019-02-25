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
  updatedAt: 1550261344272,
  currentState: 'Unpublished',
  targetState: 'Unpublished',
  name: 'aaa',
  isDraft: false,
};

const i18nTextHistoryMenuReplaceDraft = 'Replace Draft';
const i18nTextHistoryMenuUnpublish = 'Unpublish';
const i18nTextLastPublished = 'Last published on ';
const i18nTextTitle = 'Integration Detail';
const i18nTextVersion = 'Version';

stories.add(
  'published',
  withNotes('Verify there is a list of history items')(() => (
    <IntegrationDetailHistoryListViewItem
      integrationIsDraft={integrationPublished.isDraft}
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
      i18nTextTitle={text('i18nTextTitle', i18nTextTitle)}
      i18nTextVersion={text('i18nTextVersion', i18nTextVersion)}
    />
  ))
);
