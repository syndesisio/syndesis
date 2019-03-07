import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationDetailTab } from '../../src';

const stories = storiesOf('Integration/IntegrationDetailTab', module);

const i18nTextBtnEdit = 'Edit';
const i18nTextBtnPublish = 'Publish';
const i18nTextDraft = 'Draft';
const i18nTextHistory = 'History';
/***
const i18nTextHistoryMenuReplaceDraft = 'Replace Draft';
const i18nTextHistoryMenuUnpublish = 'Unpublish';
const i18nTextLastPublished = 'Last published on ';
const i18nTextVersion = 'Version';
**/

const integrationUnpublished = {
  id: 'i-LYmlhVFB6pKKaBQVSyez',
  version: 1,
  updatedAt: 'Feb 25, 2019, 11:42:21',
  currentState: 'Unpublished',
  targetState: 'Unpublished',
  name: 'Test Unpublished Integration',
  isDraft: true,
};

stories.add(
  'detail tab',
  withNotes('Verify the Detail tab contents are being displayed')(() => (
    <IntegrationDetailTab
      integrationIsDraft={integrationUnpublished.isDraft}
      i18nTextBtnEdit={text('i18nTextBtnEdit', i18nTextBtnEdit)}
      i18nTextBtnPublish={text('i18nTextBtnPublish', i18nTextBtnPublish)}
      i18nTextDraft={text('i18nTextDraft', i18nTextDraft)}
      i18nTextHistory={text('i18nTextHistory', i18nTextHistory)}
    />
  ))
);
