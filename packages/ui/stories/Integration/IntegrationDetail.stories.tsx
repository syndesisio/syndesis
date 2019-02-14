import { ListViewItem } from 'patternfly-react';
import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { StoryHelper } from '../../.storybook/StoryHelper';
import { IntegrationDetail } from '../../src';

const stories = storiesOf('Integration/IntegrationDetail', module);
const storyNotes = 'Integration Detail';

const integrationId = 'i-LUF4Pfwxo4Wcrbyt7YIz';
const integrationDescription = 'A test integration.';
const integrationExternalUrl =
  'https://i-swagger-connections-syndesis-staging.b6ff.rh-idev.openshiftapps.com';
const integrationName = 'Student API';
const integrationStatus = 'Published';
const integrationVersion = '1';

const textBtnEdit = 'Edit';
const textBtnPublish = 'Publish';
const textCopyToClipboard = 'Copy to Clipboard';
const textDraft = 'Draft';
const textExternalUrl = 'External URL';
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

const historyItems = [
  <ListViewItem
    key={1}
    i18nTextBtnEdit={textBtnEdit}
    i18nTextBtnPublish={textBtnPublish}
    i18nTextHistoryMenuReplaceDraft={textHistoryMenuReplaceDraft}
    i18nTextHistoryMenuUnpublish={textHistoryMenuUnpublish}
    i18nTextLastPublished={textLastPublished}
  />,
];

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add(
    'published',
    withNotes(storyNotes)(() => (
      <IntegrationDetail
        integrationId={text('integrationId', integrationId)}
        integrationName={text('integrationName', integrationName)}
        integrationDescription={text(
          'integrationDescription',
          integrationDescription
        )}
        integrationExternalUrl={
          (text('integrationExternalUrl'), integrationExternalUrl)
        }
        integrationStatus={text('integrationStatus', integrationStatus)}
        integrationVersion={text('integrationVersion', integrationVersion)}
        children={historyItems}
        i18nTextBtnEdit={textBtnEdit}
        i18nTextBtnPublish={textBtnPublish}
        i18nTextCopyToClipboard={textCopyToClipboard}
        i18nTextDraft={textDraft}
        i18nTextExternalUrl={textExternalUrl}
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
  )

  .add(
    'unpublished',
    withNotes(storyNotes)(() => (
      <IntegrationDetail
        integrationId={text('integrationId', integrationId)}
        integrationName={text('integrationName', integrationName)}
        integrationDescription={text(
          'integrationDescription',
          integrationDescription
        )}
        integrationStatus={text('integrationStatus', 'Unpublished')}
        integrationVersion={text('integrationVersion', integrationVersion)}
        children={historyItems}
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
