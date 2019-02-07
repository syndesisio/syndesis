import * as React from 'react';
import { storiesOf } from '@storybook/react';
import { StoryHelper } from '../../.storybook/StoryHelper';
import { IntegrationDetail } from '../../src';
// import { action } from '@storybook/addon-actions';

export const integrationDetailStory = 'example usage';

const stories = storiesOf('Integration/IntegrationDetail', module);

const storyNotes = 'Integration Detail';

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add(
    integrationDetailStory,
    withNotes(storyNotes)(() => (
      <IntegrationDetail
        i18nHistory={'History'}
        i18nLastPublished={'Last published'}
        i18nVersion={'Version'}
        i18nTitle={'Integration Detail'}
      />
    ))
  );
