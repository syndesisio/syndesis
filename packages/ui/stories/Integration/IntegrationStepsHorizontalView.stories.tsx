import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationStepsHorizontalView } from '../../src';

const stories = storiesOf('Integration/IntegrationStepsHorizontalView', module);

const integrationRunning = {
  description: 'An example integration that is running.',
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
    <IntegrationStepsHorizontalView
      description={integrationRunning.description}
      steps={integrationRunning.steps}
    />
  ))
);
