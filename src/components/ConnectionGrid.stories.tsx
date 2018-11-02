import { storiesOf } from '@storybook/react';
import React from 'react';
import { ConnectionsGrid } from './ConnectionsGrid';
import { StoryHelper } from './StoryHelper';

storiesOf('ConnectionGrid', module)
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add('a loading list of connections', function() {
    return <ConnectionsGrid loading={true} connections={[]} />;
  });
