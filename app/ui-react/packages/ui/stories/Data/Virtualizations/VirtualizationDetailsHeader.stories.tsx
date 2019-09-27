import { withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { VirtualizationDetailsHeader } from '../../../src';

const stories = storiesOf('Data/Virtualizations/VirtualizationDetailsHeader', module);
stories.addDecorator(withKnobs);

const virtualizationDescription = 'This is my MyVirt virtualization';
const virtualizationName = 'MyVirt';
const descriptionPlaceholder = 'Enter a description...';
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

const changeDescription = (/*newDescription: string*/) => {
  return Promise.resolve(true);
};

stories.add('No Description', () => (
  <VirtualizationDetailsHeader
    i18nDescriptionPlaceholder={descriptionPlaceholder}
    i18nDraft={'Draft'}
    i18nError={'Error'}
    i18nInUseText={'Used by x integrations'}
    i18nPublished={'Published'}
    i18nPublishInProgress={'Publish in progres...'}
    i18nUnpublishInProgress={'Unpublish in progres...'}
    i18nPublishLogUrlText={'View Logs'}
    publishedState={'RUNNING'}
    virtualizationName={virtualizationName}
    isWorking={false}
    onChangeDescription={changeDescription}
  />
))

.add('With Description', () => (
  <VirtualizationDetailsHeader
    i18nDescriptionPlaceholder={descriptionPlaceholder}
    i18nDraft={'Draft'}
    i18nError={'Error'}
    i18nInUseText={'Used by x integrations'}
    i18nPublished={'Published'}
    i18nPublishInProgress={'Publish in progres...'}
    i18nUnpublishInProgress={'Unpublish in progres...'}
    i18nPublishLogUrlText={'View Logs'}
    publishedState={'NOTFOUND'}
    virtualizationName={virtualizationName}
    virtualizationDescription={virtualizationDescription}
    isWorking={false}
    onChangeDescription={changeDescription}
  />
))

.add('PublishSubmitted', () => (
  <VirtualizationDetailsHeader
    i18nDescriptionPlaceholder={descriptionPlaceholder}
    i18nDraft={'Draft'}
    i18nError={'Error'}
    i18nInUseText={'Used by x integrations'}
    i18nPublished={'Published'}
    i18nPublishInProgress={'Publish in progres...'}
    i18nUnpublishInProgress={'Unpublish in progres...'}
    i18nPublishLogUrlText={'View Logs'}
    publishedState={'SUBMITTED'}
    virtualizationName={virtualizationName}
    virtualizationDescription={virtualizationDescription}
    isWorking={false}
    onChangeDescription={changeDescription}
  />
))
.add('Configuring', () => (
  <VirtualizationDetailsHeader
    i18nDescriptionPlaceholder={descriptionPlaceholder}
    i18nDraft={'Draft'}
    i18nError={'Error'}
    i18nInUseText={'Used by x integrations'}
    i18nPublished={'Published'}
    i18nPublishInProgress={'Publish in progres...'}
    i18nUnpublishInProgress={'Unpublish in progres...'}
    i18nPublishLogUrlText={'View Logs'}
    publishedState={'CONFIGURING'}
    publishingCurrentStep={publishStepConfiguring.stepNumber}
    publishingLogUrl={publishLogUrl}
    publishingTotalSteps={4}
    publishingStepText={publishStepConfiguring.stepText}
    virtualizationName={virtualizationName}
    virtualizationDescription={virtualizationDescription}
    isWorking={false}
    onChangeDescription={changeDescription}
  />
))
.add('Building', () => (
  <VirtualizationDetailsHeader
    i18nDescriptionPlaceholder={descriptionPlaceholder}
    i18nDraft={'Draft'}
    i18nError={'Error'}
    i18nInUseText={'Used by x integrations'}
    i18nPublished={'Published'}
    i18nPublishInProgress={'Publish in progres...'}
    i18nUnpublishInProgress={'Unpublish in progres...'}
    i18nPublishLogUrlText={'View Logs'}
    publishedState={'BUILDING'}
    publishingCurrentStep={publishStepBuilding.stepNumber}
    publishingLogUrl={publishLogUrl}
    publishingTotalSteps={4}
    publishingStepText={publishStepBuilding.stepText}
    virtualizationName={virtualizationName}
    virtualizationDescription={virtualizationDescription}
    isWorking={false}
    onChangeDescription={changeDescription}
  />
))
.add('Deploying', () => (
  <VirtualizationDetailsHeader
    i18nDescriptionPlaceholder={descriptionPlaceholder}
    i18nDraft={'Draft'}
    i18nError={'Error'}
    i18nInUseText={'Used by x integrations'}
    i18nPublished={'Published'}
    i18nPublishInProgress={'Publish in progres...'}
    i18nUnpublishInProgress={'Unpublish in progres...'}
    i18nPublishLogUrlText={'View Logs'}
    publishedState={'DEPLOYING'}
    publishingCurrentStep={publishStepDeploying.stepNumber}
    publishingLogUrl={publishLogUrl}
    publishingTotalSteps={4}
    publishingStepText={publishStepDeploying.stepText}
    virtualizationName={virtualizationName}
    virtualizationDescription={virtualizationDescription}
    isWorking={false}
    onChangeDescription={changeDescription}
  />
))
.add('Published With OdataUrl', () => (
  <VirtualizationDetailsHeader
    i18nDescriptionPlaceholder={descriptionPlaceholder}
    i18nDraft={'Draft'}
    i18nError={'Error'}
    i18nInUseText={'Used by x integrations'}
    i18nPublished={'Published'}
    i18nPublishInProgress={'Publish in progres...'}
    i18nUnpublishInProgress={'Unpublish in progres...'}
    i18nPublishLogUrlText={'View Logs'}
    publishedState={'RUNNING'}
    odataUrl={'http://odataUrl.com'}
    virtualizationName={virtualizationName}
    virtualizationDescription={virtualizationDescription}
    isWorking={false}
    onChangeDescription={changeDescription}
  />
));
