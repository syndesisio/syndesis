import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  IIntegrationDetailMetricsProps,
  IntegrationDetailMetrics,
} from '../../../src';

const stories = storiesOf(
  'Integration/Metrics/IntegrationDetailMetrics',
  module
);

const uptimeDuration = '3:15:59';
const errors = 2;
const i18nLastProcessed = 'Last Processed';
const i18nNoDataAvailable = 'No data available';
const i18nSince = 'Since ';
const i18nTotalErrors = 'Total Errors';
const i18nTotalMessages = 'Total Messages';
const i18nUptime = 'Uptime';
const lastProcessed = '2 May 2019 08:19:42 GMT';
const messages = 26126;
const start = 2323342333;

const propsOnlyRequired = {
  i18nLastProcessed,
  i18nNoDataAvailable,
  i18nSince,
  i18nTotalErrors,
  i18nTotalMessages,
  i18nUptime,
} as IIntegrationDetailMetricsProps;

const propsAll = {
  ...propsOnlyRequired,
  uptimeDuration,
  errors,
  lastProcessed,
  messages,
  start,
} as IIntegrationDetailMetricsProps;

stories

  .add('empty state', () => <IntegrationDetailMetrics {...propsOnlyRequired} />)

  .add('all props', () => <IntegrationDetailMetrics {...propsAll} />);
