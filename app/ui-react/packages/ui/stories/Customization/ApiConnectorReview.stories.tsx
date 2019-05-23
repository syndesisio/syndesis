import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ApiConnectorReview } from '../../src';

const stories = storiesOf(
  'Customization/ApiClientConnector/ApiConnectorReview',
  module
);

stories.add('with description', () => (
  <ApiConnectorReview
    apiConnectorDescription={'the description'}
    apiConnectorName={'theName'}
    i18nApiDefinitionHeading={'API DEFINITION'}
    i18nDescriptionLabel={'Description'}
    i18nImportedHeading={'IMPORTED'}
    i18nNameLabel={'Name'}
    i18nOperationsHtmlMessage={'<strong>10</strong> operations'}
    i18nTitle={'Review Actions'}
  />
));

stories.add('no description', () => (
  <ApiConnectorReview
    apiConnectorName={'theName'}
    i18nApiDefinitionHeading={'API DEFINITION'}
    i18nDescriptionLabel={'Description'}
    i18nImportedHeading={'IMPORTED'}
    i18nNameLabel={'Name'}
    i18nOperationsHtmlMessage={'<strong>0</strong> operations'}
    i18nTitle={'Review Actions'}
  />
));

stories.add('tagged messages', () => (
  <ApiConnectorReview
    apiConnectorDescription={'the description'}
    apiConnectorName={'theName'}
    i18nApiDefinitionHeading={'API DEFINITION'}
    i18nDescriptionLabel={'Description'}
    i18nImportedHeading={'IMPORTED'}
    i18nNameLabel={'Name'}
    i18nOperationsHtmlMessage={'<strong>49</strong> operations'}
    i18nOperationTagHtmlMessages={[
      '- <strong>1</strong> tagged KIE Server Script :: Core',
      '- <strong>23</strong> tagged User tasks administration :: BPM',
      '- <strong>24</strong> tagged Queries - processes, nodes, variables and tasks :: BPM',
      '- <strong>1</strong> tagged Rules evaluation :: Core',
    ]}
    i18nTitle={'Review Actions'}
  />
));

stories.add('errors and warnings', () => (
  <ApiConnectorReview
    apiConnectorDescription={'the description'}
    apiConnectorName={'theName'}
    errorMessages={[
      'this is an error message',
      'this is another error message',
      'this is the last error message',
    ]}
    i18nApiDefinitionHeading={'API DEFINITION'}
    i18nDescriptionLabel={'Description'}
    i18nErrorsHeading={'Errors'}
    i18nImportedHeading={'IMPORTED'}
    i18nNameLabel={'Name'}
    i18nOperationsHtmlMessage={'<strong>10</strong> operations'}
    i18nTitle={'Review Actions'}
    i18nWarningsHeading={'Warnings'}
    warningMessages={[
      'this is a warning message',
      'this is another warning message',
      'this is the last warning message',
    ]}
  />
));

stories.add('validation fallback', () => (
  <ApiConnectorReview
    apiConnectorName={'theName'}
    i18nApiDefinitionHeading={'API DEFINITION'}
    i18nDescriptionLabel={'Description'}
    i18nImportedHeading={'IMPORTED'}
    i18nNameLabel={'Name'}
    i18nOperationsHtmlMessage={'<strong>0</strong> operations'}
    i18nTitle={'Review Actions'}
    i18nValidationFallbackMessage={
      'No connector details are currently available.'
    }
  />
));
