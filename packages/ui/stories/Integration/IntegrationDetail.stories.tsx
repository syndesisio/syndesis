import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationDetailTab } from '../../src';

const stories = storiesOf('Integration/IntegrationDetail', module);

const i18nTextBtnEdit = 'Edit';
const i18nTextBtnPublish = 'Publish';
const i18nTextDraft = 'Draft';
const i18nTextHistory = 'History';

const integration = {
  description: 'An example integration that is running.',
  isDraft: true,
  steps: [
    {
      name: 'SQL',
      pattern: 'From',
    },
    {
      name: 'Salesforce',
      pattern: 'To',
    },
  ],
};

stories.add(
  'detail tab',
  withNotes('Verify the Detail tab contents are being displayed')(() => (
    <IntegrationDetailTab
      description={integration.description}
      steps={integration.steps}
      integrationIsDraft={integration.isDraft}
      i18nTextBtnEdit={text('i18nTextBtnEdit', i18nTextBtnEdit)}
      i18nTextBtnPublish={text('i18nTextBtnPublish', i18nTextBtnPublish)}
      i18nTextDraft={text('i18nTextDraft', i18nTextDraft)}
      i18nTextHistory={text('i18nTextHistory', i18nTextHistory)}
    />
  ))
);
