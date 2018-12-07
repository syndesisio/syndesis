import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { text } from '@storybook/addon-knobs';
import { StoryHelper } from '../.storybook/StoryHelper';
import { IntegrationStatusDetail } from '../src';

const stories = storiesOf('Components', module);

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add('IntegrationStatusDetail - Without Detail', () => (
    <IntegrationStatusDetail
      targetState={text('targetState', 'Published')}
      i18nProgressPending={'Pending'}
      i18nProgressStarting={'Starting...'}
      i18nProgressStopping={'Stopping...'}
      i18nLogUrlText={'View Log'}
    />
  ))
  .add('IntegrationStatusDetail - With Detail', () => (
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
