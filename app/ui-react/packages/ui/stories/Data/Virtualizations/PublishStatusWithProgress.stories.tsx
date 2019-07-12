import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { PublishStatusWithProgress } from '../../../src';

const stories = storiesOf(
  'Data/Virtualizations/PublishStatusWithProgress',
  module
);

const errorText = 'Error';
const publishedText = 'Published';
const unpublishedText = 'Draft';
const publishInProgressText = 'publish in progress...';
const unpublishInProgressText = 'unpublish in progress...';
const publishLogUrlText = 'View Logs';
const publishLogUrl = 'http://redhat.com';

const publishStepConfiguring = {
  stepNumber: 1,
  stepText: 'Configuring',
};
const publishStepBuilding = {
  stepNumber: 2,
  stepText: 'Building',
};
const publishStepDeploying = {
  stepNumber: 3,
  stepText: 'Deploying',
};

stories

  .add('published state', () => (
    <PublishStatusWithProgress
      publishedState={'RUNNING'}
      i18nError={errorText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublishLogUrlText={publishLogUrlText}
    />
  ))
  .add('draft state', () => (
    <PublishStatusWithProgress
      publishedState={'NOTFOUND'}
      i18nError={errorText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublishLogUrlText={publishLogUrlText}
    />
  ))
  .add('publish submitted state', () => (
    <PublishStatusWithProgress
      publishedState={'SUBMITTED'}
      i18nError={errorText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublishLogUrlText={publishLogUrlText}
    />
  ))
  .add('unpublish submitted state', () => (
    <PublishStatusWithProgress
      publishedState={'DELETE_SUBMITTED'}
      i18nError={errorText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublishLogUrlText={publishLogUrlText}
    />
  ))
  .add('configuring state', () => (
    <PublishStatusWithProgress
      publishedState={'CONFIGURING'}
      i18nError={errorText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublishLogUrlText={publishLogUrlText}
      publishingCurrentStep={publishStepConfiguring.stepNumber}
      publishingLogUrl={publishLogUrl}
      publishingTotalSteps={4}
      publishingStepText={publishStepConfiguring.stepText}
    />
  ))
  .add('building state', () => (
    <PublishStatusWithProgress
      publishedState={'BUILDING'}
      i18nError={errorText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublishLogUrlText={publishLogUrlText}
      publishingCurrentStep={publishStepBuilding.stepNumber}
      publishingLogUrl={publishLogUrl}
      publishingTotalSteps={4}
      publishingStepText={publishStepBuilding.stepText}
    />
  ))
  .add('deploying state', () => (
    <PublishStatusWithProgress
      publishedState={'DEPLOYING'}
      i18nError={errorText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublishLogUrlText={publishLogUrlText}
      publishingCurrentStep={publishStepDeploying.stepNumber}
      publishingLogUrl={publishLogUrl}
      publishingTotalSteps={4}
      publishingStepText={publishStepDeploying.stepText}
    />
  ));
