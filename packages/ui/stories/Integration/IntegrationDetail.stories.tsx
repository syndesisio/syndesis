import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { StoryHelper } from '../../.storybook/StoryHelper';
import { IntegrationDetail } from '../../src';

const stories = storiesOf('Integration/IntegrationDetail', module);

const integrationDetailStory = 'sample integration detail page';
const storyNotes = 'Integration Detail';

const integrationId = '';
const integrationDescription = '';
const integrationName = '';
const integrationStatus = '';
const integrationVersion = '';

const textBtnEdit = 'Edit';
const textBtnPublish = 'Publish';
const textDraft = 'Draft';
const textHistory = 'History';
const textHistoryMenuReplaceDraft = 'Replace Draft';
const textHistoryMenuUnpublish = 'Unpublish';
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
        integrationId={text('integrationId', integrationId)}
        integrationName={text('integrationName', integrationName)}
        integrationDescription={text(
          'integrationDescription',
          integrationDescription
        )}
        integrationStatus={text('integrationStatus', integrationStatus)}
        integrationVersion={text('integrationVersion', integrationVersion)}
        i18nTextBtnEdit={textBtnEdit}
        i18nTextBtnPublish={textBtnPublish}
        i18nTextDraft={textDraft}
        i18nTextHistory={textHistory}
        i18nTextHistoryMenuReplaceDraft={textHistoryMenuReplaceDraft}
        i18nTextHistoryMenuUnpublish={textHistoryMenuUnpublish}
        i18nTextLastPublished={textLastPublished}
        i18nTextNoDescription={textNoDescription}
        i18nTextTabActivity={textTabActivity}
        i18nTextTabDetails={textTabDetails}
        i18nTextTableMetrics={textTabMetrics}
        i18nTextTitle={textTitle}
        i18nTextVersion={textVersion}
      />
    ))
  );
