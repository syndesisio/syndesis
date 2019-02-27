import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

const stories = storiesOf('Integration/IntegrationStepsHorizontalView', module);

const integrationRunning = {
  steps: [
    {
      name: 'Timer',
    },
    {
      name: 'Log',
    },
  ],
};

// const integrationStopped = {};

stories
  .add(
    'running',
    withNotes('Verify the integration is running')(() => (
      <>
        <p>{integrationRunning.steps[0].name}</p>
      </>
    ))
  )

  .add('stopped', withNotes('Verify the integration is stopped')(() => <></>));
