import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationStepsHorizontalView } from '../../src';

const stories = storiesOf(
  'Integration/Detail/Components/IntegrationStepsHorizontalView',
  module
);

const integrationRunning = {
  steps: [
    {
      name: 'SQL',
      pattern: 'From',
    },
    {
      name: 'Salesforce',
      pattern: 'To',
    },
  ],
};

stories.add(
  'running',
  withNotes('Verify the integration is running')(() => (
    <IntegrationStepsHorizontalView steps={integrationRunning.steps} />
  ))
);
