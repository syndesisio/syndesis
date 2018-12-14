import * as React from 'react';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import { StoryHelper } from '../../.storybook/StoryHelper';
import { CustomizationsExtensionImport } from '../../src/Customizations';

export const extensionImportStory = 'story-tbd';

const stories = storiesOf('CustomizationsExtensionImport', module);
const storyNotes = '- Verify something here';

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add(
    extensionImportStory,
    withNotes(storyNotes)(() => (
      <CustomizationsExtensionImport i18nTitle={'Import Extension'} />
    ))
  );
