// import { object } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationStepsHorizontalView } from '../../src';

const stories = storiesOf('Integration/IntegrationStepsHorizontalView', module);

const integrationRunning = {
  steps: [
    {
      id: 'd2D#T^g',
      name: 'Timer',
    },
    {
      id: 'In2!%@s',
      name: 'Log',
    },
  ],
};

// const integrationStopped = {};

stories
  .add(
    'running',
    withNotes('Verify the integration is running')(() => (
      <IntegrationStepsHorizontalView steps={integrationRunning.steps} />
    ))
  )

  .add(
    'stopped',
    withNotes('Verify the integration is stopped')(() => (
      <IntegrationStepsHorizontalView />
    ))
  );
