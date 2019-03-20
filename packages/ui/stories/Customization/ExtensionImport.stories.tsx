import * as React from 'react';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import { ExtensionImport } from '../../src';

export const extensionImportStory = 'story-tbd';

const stories = storiesOf(
  'Customization/Extensions/Component/ExtensionImport',
  module
);
const storyNotes = '- Verify something here';

stories.add(
  extensionImportStory,
  withNotes(storyNotes)(() => <ExtensionImport />)
);
