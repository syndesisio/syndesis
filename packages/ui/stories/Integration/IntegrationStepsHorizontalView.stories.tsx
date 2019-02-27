import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

const stories = storiesOf('Integration/IntegrationStepsHorizontalView', module);

stories.add(
  'running',
  withNotes('Verify the integration is running')(() => <p>Placeholder</p>)
);
