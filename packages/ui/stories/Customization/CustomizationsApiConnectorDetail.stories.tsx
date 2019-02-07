import * as React from 'react';
import { storiesOf } from '@storybook/react';
import { StoryHelper } from '../../.storybook/StoryHelper';
import { CustomizationsApiConnectorDetail } from '../../src';

export const apiConnectorDetailsStory = 'story-tbd';

const stories = storiesOf(
  'Customization/CustomizationsApiConnectorDetail',
  module
);

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
