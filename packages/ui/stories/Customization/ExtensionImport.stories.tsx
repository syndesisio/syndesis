import * as React from 'react';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import { StoryHelper } from '../../.storybook/StoryHelper';
import { ExtensionImport } from '../../src';

export const extensionImportStory = 'story-tbd';

const stories = storiesOf('Customization/ExtensionImport', module);
const storyNotes = '- Verify something here';

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add(
    extensionImportStory,
    withNotes(storyNotes)(() => (
      <ExtensionImport i18nTitle={'Import Extension'} />
    ))
  );
