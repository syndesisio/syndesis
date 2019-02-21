import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationDetailHistoryListViewItem } from '../../src';

const stories = storiesOf(
  'Integration/IntegrationDetailHistoryListViewItem',
  module
);

const integrationVersion = '1';
const i18nTextBtnPublish = 'string';

const i18nTextTitle = 'Title';
const i18nTextVersion = 'Version';

stories
  .add(
    'published',
    withNotes('Verify there is no Publish button')(() => (
      <IntegrationDetailHistoryListViewItem
        integrationVersion={text('integrationVersion', integrationVersion)}
        i18nTextTitle={text('i18nTextTitle', i18nTextTitle)}
        i18nTextVersion={text('i18nTextVersion', i18nTextVersion)}
        i18nTextBtnPublish={text('i18nTextBtnPublish', i18nTextBtnPublish)}
      />
    ))
  )
  .add(
    'unpublished',
    withNotes('Verify there is a Draft button')(() => (
      <IntegrationDetailHistoryListViewItem
        integrationVersion={text('integrationVersion', integrationVersion)}
        i18nTextTitle={text('i18nTextTitle', i18nTextTitle)}
        i18nTextVersion={text('i18nTextVersion', i18nTextVersion)}
        i18nTextBtnPublish={text('i18nTextBtnPublish', i18nTextBtnPublish)}
      />
    ))
  );
