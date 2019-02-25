import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationDetailHistoryListView } from '../../src';

const stories = storiesOf(
  'Integration/IntegrationDetailHistoryListView',
  module
);

const i18nTextDraft = 'Draft';
const i18nTextHistory = 'History';

stories.add(
  'has history',
  withNotes('Verify there is a list of history items')(() => (
    <IntegrationDetailHistoryListView
      i18nTextDraft={text('i18nTextDraft', i18nTextDraft)}
      i18nTextHistory={text('i18nTextHistory', i18nTextHistory)}
    />
  ))
);
