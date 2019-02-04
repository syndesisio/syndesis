import * as React from 'react';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import { StoryHelper } from '../../.storybook/StoryHelper';
import { IntegrationDetailHistory } from '../../src';

export const integrationDetailHistoryStory = 'sample usage';

const stories = storiesOf('Integration/IntegrationDetailHistory', module);
const storyNotes = '- Verify something here';

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add(
    integrationDetailHistoryStory,
    withNotes(storyNotes)(() => (
      <IntegrationDetailHistory i18nTitle={'Integration Detail History'} />
    ))
  );
