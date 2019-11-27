import { Bullseye } from '@patternfly/react-core';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { PublishStatusWithProgress } from '../../../src';

const stories = storiesOf(
  'Data/Virtualizations/PublishStatusWithProgress',
  module
);

stories

  .add('published', () => (
    <Bullseye style={{ margin: 40 }}>
      <PublishStatusWithProgress
        i18nPublishLogUrlText={'log/url/goes/here'}
        i18nPublishState={'Running'}
        i18nPublishVersion={2}
        isProgressWithLink={false}
        inListView={false}
        labelType={'primary'}
      />
    </Bullseye>
  ))
  .add('publish in-progress', () => (
    <Bullseye style={{ margin: 40 }}>
      <PublishStatusWithProgress
        i18nPublishLogUrlText={'View Log'}
        i18nPublishState={'Publishing...'}
        i18nPublishVersion={2}
        isProgressWithLink={false}
        inListView={false}
        labelType={'default'}
      />
    </Bullseye>
  ))
  .add('publish step', () => (
    <Router>
      <Bullseye style={{ margin: 40 }}>
        <PublishStatusWithProgress
          isProgressWithLink={true}
          inListView={false}
          i18nPublishState={'BUILDING'}
          i18nPublishLogUrlText={'View Log'}
          i18nPublishVersion={2}
          labelType={'default'}
          publishingCurrentStep={1}
          publishingLogUrl={'log/url/goes/here'}
          publishingTotalSteps={4}
          publishingStepText={'Building'}
        />
      </Bullseye>
    </Router>
  ));
