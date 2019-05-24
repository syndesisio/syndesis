import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { VirtualizationPublishStatusDetail } from '../../../src';

const stories = storiesOf(
  'Data/Virtualizations/VirtualizationPublishStatusDetail',
  module
);

const logsLink = 'linkText';
const publishInProgress = 'Publish In Progress...';
const logUrlText = 'View Logs';

stories

  .add('without logs link', () => (
    <VirtualizationPublishStatusDetail
      currentStep={2}
      totalSteps={4}
      stepText={'Building'}
      i18nPublishInProgress={publishInProgress}
      i18nLogUrlText={logUrlText}
    />
  ))
  .add('with logs link', () => (
    <VirtualizationPublishStatusDetail
      currentStep={2}
      totalSteps={4}
      stepText={'Building'}
      logUrl={logsLink}
      i18nPublishInProgress={publishInProgress}
      i18nLogUrlText={logUrlText}
    />
  ));
