import { Bullseye } from '@patternfly/react-core';
import { boolean, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { VirtualizationDetailsHeader } from '../../../src';

const stories = storiesOf(
  'Data/Virtualizations/VirtualizationDetailsHeader',
  module
);
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

stories
  .add('No Description', () => (
    <Bullseye style={{ margin: 100 }}>
      <VirtualizationDetailsHeader
        isProgressWithLink={false}
        i18nPublishState={'Running'}
        labelType={'primary'}
        i18nDescriptionPlaceholder={descriptionPlaceholder}
        i18nPublishLogUrlText={'View Logs'}
        i18nODataUrlText={'View OData'}
        modified={boolean('modified', false)}
        publishedState={'RUNNING'}
        publishedVersion={3}
        virtualizationName={virtualizationName}
        isWorking={false}
        onChangeDescription={changeDescription}
      />
    </Bullseye>
  ))

  .add('With Description', () => (
    <Bullseye style={{ margin: 100 }}>
      <VirtualizationDetailsHeader
        isProgressWithLink={false}
        i18nPublishState={'Draft'}
        labelType={'default'}
        i18nDescriptionPlaceholder={descriptionPlaceholder}
        i18nPublishLogUrlText={'View Logs'}
        i18nODataUrlText={'View OData'}
        modified={boolean('modified', false)}
        publishedState={'NOTFOUND'}
        virtualizationName={virtualizationName}
        virtualizationDescription={virtualizationDescription}
        isWorking={false}
        onChangeDescription={changeDescription}
      />
    </Bullseye>
  ))

  .add('Publish Submitted', () => (
    <Bullseye style={{ margin: 100 }}>
      <VirtualizationDetailsHeader
        isProgressWithLink={false}
        i18nDescriptionPlaceholder={descriptionPlaceholder}
        i18nPublishLogUrlText={'View Logs'}
        i18nODataUrlText={'View OData'}
        i18nPublishState={'Publishing...'}
        labelType={'default'}
        modified={boolean('modified', false)}
        publishedState={'SUBMITTED'}
        publishedVersion={3}
        virtualizationName={virtualizationName}
        virtualizationDescription={virtualizationDescription}
        isWorking={false}
        onChangeDescription={changeDescription}
      />
    </Bullseye>
  ))
  .add('Configuring', () => (
    <Bullseye style={{ margin: 100 }}>
      <VirtualizationDetailsHeader
        isProgressWithLink={true}
        i18nDescriptionPlaceholder={descriptionPlaceholder}
        i18nPublishLogUrlText={'View Logs'}
        i18nODataUrlText={'View OData'}
        i18nPublishState={'Publishing...'}
        labelType={'default'}
        modified={boolean('modified', false)}
        publishedState={'CONFIGURING'}
        publishedVersion={3}
        publishingCurrentStep={publishStepConfiguring.stepNumber}
        publishingLogUrl={publishLogUrl}
        publishingTotalSteps={4}
        publishingStepText={publishStepConfiguring.stepText}
        virtualizationName={virtualizationName}
        virtualizationDescription={virtualizationDescription}
        isWorking={false}
        onChangeDescription={changeDescription}
      />
    </Bullseye>
  ))
  .add('Building', () => (
    <Bullseye style={{ margin: 100 }}>
      <VirtualizationDetailsHeader
        isProgressWithLink={true}
        i18nDescriptionPlaceholder={descriptionPlaceholder}
        i18nPublishLogUrlText={'View Logs'}
        i18nODataUrlText={'View OData'}
        i18nPublishState={'Publishing...'}
        labelType={'default'}
        modified={boolean('modified', false)}
        publishedState={'BUILDING'}
        publishedVersion={3}
        publishingCurrentStep={publishStepBuilding.stepNumber}
        publishingLogUrl={publishLogUrl}
        publishingTotalSteps={4}
        publishingStepText={publishStepBuilding.stepText}
        virtualizationName={virtualizationName}
        virtualizationDescription={virtualizationDescription}
        isWorking={false}
        onChangeDescription={changeDescription}
      />
    </Bullseye>
  ))
  .add('Deploying', () => (
    <Bullseye style={{ margin: 100 }}>
      <VirtualizationDetailsHeader
        isProgressWithLink={true}
        i18nDescriptionPlaceholder={descriptionPlaceholder}
        i18nPublishLogUrlText={'View Logs'}
        i18nODataUrlText={'View OData'}
        i18nPublishState={'Publishing...'}
        labelType={'default'}
        modified={boolean('modified', false)}
        publishedState={'DEPLOYING'}
        publishedVersion={3}
        publishingCurrentStep={publishStepDeploying.stepNumber}
        publishingLogUrl={publishLogUrl}
        publishingTotalSteps={4}
        publishingStepText={publishStepDeploying.stepText}
        virtualizationName={virtualizationName}
        virtualizationDescription={virtualizationDescription}
        isWorking={false}
        onChangeDescription={changeDescription}
      />
    </Bullseye>
  ))
  .add('Published With OdataUrl', () => (
    <Bullseye style={{ margin: 100 }}>
      <VirtualizationDetailsHeader
        isProgressWithLink={false}
        i18nPublishState={'Published'}
        labelType={'primary'}
        i18nDescriptionPlaceholder={descriptionPlaceholder}
        i18nPublishLogUrlText={'View Logs'}
        i18nODataUrlText={'View OData'}
        modified={boolean('modified', false)}
        publishedState={'RUNNING'}
        publishedVersion={3}
        odataUrl={'http://odataUrl.com'}
        virtualizationName={virtualizationName}
        virtualizationDescription={virtualizationDescription}
        isWorking={false}
        onChangeDescription={changeDescription}
      />
    </Bullseye>
  ));
