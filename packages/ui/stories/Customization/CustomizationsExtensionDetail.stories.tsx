import * as React from 'react';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import { StoryHelper } from '../../.storybook/StoryHelper';
import { CustomizationsExtensionDetail } from '../../src';

export const extensionDetailStory = 'story-tbd';

const stories = storiesOf(
  'Customization/CustomizationsExtensionDetail',
  module
);
const storyNotes = '- Verify something here';

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add(
    extensionDetailStory,
    withNotes(storyNotes)(() => (
      <CustomizationsExtensionDetail
        extensionId={'extensionId'}
        i18nTitle={'Extension Detail'}
      />
    ))
  );
