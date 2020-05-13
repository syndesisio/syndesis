import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ApiConnectorCreatorLayout } from '../../../src';
import {
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';
import { OpenApiReviewActions } from '../../../src/Shared';

const stories = storiesOf(
  'Customization/ApiClientConnector/CreateApiConnector/2 - Imported Operations',
  module
);

stories.add('Imported Operations (REST)', () => (
  <ApiConnectorCreatorLayout
    content={
      <OpenApiReviewActions
        i18nApiDefinitionHeading={'API DEFINITION'}
        i18nDescriptionLabel={'Description'}
        i18nImportedHeading={'IMPORTED'}
        i18nNameLabel={'Name'}
        apiProviderDescription={''}
        apiProviderName={''}
        i18nOperationsHtmlMessage={`<strong>2</strong> operations`}
        i18nWarningsHeading={'WARNINGS'}
        warningMessages={[]}
        i18nErrorsHeading={'ERRORS'}
        errorMessages={[]}
      />
    }
    footer={
      <ApiConnectorCreatorFooter
        backHref={''}
        i18nBack={'Back'}
        i18nNext={'Next'}
        i18nReviewEdit={'Review/Edit'}
        isNextLoading={boolean('isNextLoading', false)}
        isNextDisabled={boolean('isNextDisabled', false)}
        nextHref={''}
        reviewEditHref={''}
      />
    }
    navigation={
      <ApiConnectorCreatorBreadSteps
        step={2}
        i18nDetails={'Review/Edit Connector Details'}
        i18nReview={'Imported Operations'}
        i18nSecurity={'Specify Security'}
        i18nSelectMethod={'Provide Document'}
      />
    }
    toggle={
      <ApiConnectorCreatorToggleList
        step={1}
        i18nDetails={'Review/Edit Connector Details'}
        i18nReview={'Imported Operations'}
        i18nSecurity={'Specify Security'}
        i18nSelectMethod={'Provide Document'}
      />
    }
  />
));

stories.add('Specify Service & Port (SOAP)', () => (
  <ApiConnectorCreatorLayout
    content={
      <OpenApiReviewActions
        i18nApiDefinitionHeading={'API DEFINITION'}
        i18nDescriptionLabel={'Description'}
        i18nImportedHeading={'IMPORTED'}
        i18nNameLabel={'Name'}
        apiProviderDescription={''}
        apiProviderName={''}
        i18nOperationsHtmlMessage={`<strong>2</strong> operations`}
        i18nWarningsHeading={'WARNINGS'}
        warningMessages={[]}
        i18nErrorsHeading={'ERRORS'}
        errorMessages={[]}
      />
    }
    footer={
      <ApiConnectorCreatorFooter
        backHref={''}
        i18nBack={'Back'}
        i18nNext={'Next'}
        i18nReviewEdit={'Review/Edit'}
        isNextLoading={boolean('isNextLoading', false)}
        isNextDisabled={boolean('isNextDisabled', false)}
        nextHref={''}
        reviewEditHref={''}
      />
    }
    navigation={
      <ApiConnectorCreatorBreadSteps
        step={2}
        i18nDetails={'Review/Edit Connector Details'}
        i18nReview={'Imported Operations'}
        i18nSecurity={'Specify Security'}
        i18nSelectMethod={'Provide Document'}
      />
    }
    toggle={
      <ApiConnectorCreatorToggleList
        step={1}
        i18nDetails={'Review/Edit Connector Details'}
        i18nReview={'Imported Operations'}
        i18nSecurity={'Specify Security'}
        i18nSelectMethod={'Provide Document'}
      />
    }
  />
));
