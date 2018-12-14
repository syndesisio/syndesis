import * as React from 'react';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import { StoryHelper } from '../../.storybook/StoryHelper';
import { CustomizationsApiConnectorDetail } from '../../src/Customizations';

export const apiConnectorDetailsStory = 'story-tbd';

const stories = storiesOf('CustomizationsApiConnectorDetail', module);

const storyNotes = '- Verify something here';

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add(
    apiConnectorDetailsStory,
    withNotes(storyNotes)(() => (
      <CustomizationsApiConnectorDetail
        apiConnectorId={'apiConnectorId'}
        i18nTitle={'API Connector Detail'}
      />
    ))
  );
