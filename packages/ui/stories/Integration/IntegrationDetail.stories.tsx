import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { StoryHelper } from '../../.storybook/StoryHelper';
import { IntegrationDetail } from '../../src';

const stories = storiesOf('Integration/IntegrationDetail', module);

const integrationDetailStory = 'sample integration detail page';
const storyNotes = 'Integration Detail';

const textBtnEdit = 'Edit';
const textBtnPublish = 'Publish';
const textDraft = 'Draft';
const textHistory = 'History';
const textLastPublished = 'Last published';
const textNoDescription = 'No description set...';
const textTabActivity = 'Activity';
const textTabDetails = 'Details';
const textTabMetrics = 'Metrics';
const textTitle = 'Integration Detail';
const textVersion = 'Version';

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add(
    integrationDetailStory,
    withNotes(storyNotes)(() => (
      <IntegrationDetail
        integrationId={'integrationId'}
        i18nBtnEdit={textBtnEdit}
        i18nBtnPublish={textBtnPublish}
        i18nDraft={textDraft}
        i18nHistory={textHistory}
        i18nLastPublished={textLastPublished}
        i18nNoDescription={textNoDescription}
        i18nTabActivity={textTabActivity}
        i18nTabDetails={textTabDetails}
        i18nTableMetrics={textTabMetrics}
        i18nTitle={textTitle}
        i18nVersion={textVersion}
      />
    ))
  );
