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
        i18nPublishStateMessage={'The virtualization is running'}
        isProgressWithLink={false}
        inListView={false}
        labelType={'primary'}
        modified={false}
      />
    </Bullseye>
  ))
  .add('publish in-progress', () => (
    <Bullseye style={{ margin: 40 }}>
      <PublishStatusWithProgress
        i18nPublishLogUrlText={'View Log'}
        i18nPublishState={'Publishing...'}
        i18nPublishStateMessage={'The virtualization publish is in progress'}
        isProgressWithLink={false}
        inListView={false}
        labelType={'default'}
        modified={false}
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
          i18nPublishStateMessage={'The virtualization build is in progress'}
          i18nPublishLogUrlText={'View Log'}
          labelType={'default'}
          modified={false}
          publishingCurrentStep={1}
          publishingLogUrl={'log/url/goes/here'}
          publishingTotalSteps={4}
          publishingStepText={'Building'}
        />
      </Bullseye>
    </Router>
  ));
