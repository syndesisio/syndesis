import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationDetailHistoryListViewItem } from '../../src';

const stories = storiesOf(
  'Integration/IntegrationDetailHistoryListViewItem',
  module
);

const i18nTextBtnPublish = 'string';

stories
  .add(
    'published',
    withNotes('Verify there is no Publish button')(() => (
      <IntegrationDetailHistoryListViewItem
        i18nTextBtnEdit={text('i18nTextBtnPublish', i18nTextBtnPublish)}
      />
    ))
  )
  .add(
    'unpublished',
    withNotes('Verify there is a Draft button')(() => (
      <IntegrationDetailHistoryListViewItem
        i18nTextBtnEdit={text('i18nTextBtnPublish', i18nTextBtnPublish)}
      />
    ))
  );
