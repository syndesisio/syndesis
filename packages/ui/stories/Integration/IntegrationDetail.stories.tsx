import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { StoryHelper } from '../../.storybook/StoryHelper';
import { IntegrationDetail } from '../../src';

export const integrationDetailStory = 'example usage';

const stories = storiesOf('Integration/IntegrationDetail', module);

const storyNotes = 'Integration Detail';

const textHistory = 'History';
const textLastPublished = 'Last published';
const textTitle = 'Integration Detail';
const textVersion = 'Version';

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add(
    integrationDetailStory,
    withNotes(storyNotes)(() => (
      <IntegrationDetail
        i18nHistory={textHistory}
        i18nLastPublished={textLastPublished}
        i18nTitle={textTitle}
        i18nVersion={textVersion}
      />
    ))
  );
