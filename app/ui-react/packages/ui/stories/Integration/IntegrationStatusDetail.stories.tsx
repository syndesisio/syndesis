import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { IntegrationStatusDetail } from '../../src';

const stories = storiesOf('Integration/IntegrationStatusDetail', module);

stories

  .add('without detail', () => (
    <IntegrationStatusDetail
      targetState={text('targetState', 'Published')}
      i18nProgressPending={'Pending'}
      i18nProgressStarting={'Starting...'}
      i18nProgressStopping={'Stopping...'}
      i18nLogUrlText={'View Log'}
    />
  ))
  .add('with detail', () => (
    <IntegrationStatusDetail
      value={text('value', 'Building')}
      targetState={text('targetState', 'Published')}
      currentStep={text('currentStep', '2')}
      totalSteps={text('totalSteps', '4')}
      i18nProgressPending={'Pending'}
      i18nProgressStarting={'Starting...'}
      i18nProgressStopping={'Stopping...'}
      i18nLogUrlText={'View Log'}
    />
  ));
