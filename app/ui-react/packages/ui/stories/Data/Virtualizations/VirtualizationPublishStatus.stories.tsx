import { select } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { VirtualizationPublishStatus } from '../../../src';

const stories = storiesOf(
  'Data/Virtualizations/VirtualizationPublishStatus',
  module
);

const errorText = 'Error';
const publishedText = 'Published';
const unpublishedText = 'Draft';
const publishInProgressText = 'publish in progress...';
const unpublishInProgressText = 'unpublish in progress...';

stories

  .add('published state', () => (
    <VirtualizationPublishStatus
      currentState={select(
        'currentState',
        [
          'BUILDING',
          'CANCELLED',
          'CONFIGURING',
          'DEPLOYING',
          'FAILED',
          'NOTFOUND',
          'RUNNING',
          'SUBMITTED',
          'DELETE_SUBMITTED',
          'DELETE_REQUEUE',
          'DELETE_DONE',
        ],
        'RUNNING'
      )}
      i18nError={errorText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
    />
  ))
  .add('draft state', () => (
    <VirtualizationPublishStatus
      currentState={select(
        'currentState',
        [
          'BUILDING',
          'CANCELLED',
          'CONFIGURING',
          'DEPLOYING',
          'FAILED',
          'NOTFOUND',
          'RUNNING',
          'SUBMITTED',
          'DELETE_SUBMITTED',
          'DELETE_REQUEUE',
          'DELETE_DONE',
        ],
        'NOTFOUND'
      )}
      i18nError={errorText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
    />
  ))
  .add('publish submitted state', () => (
    <VirtualizationPublishStatus
      currentState={select(
        'currentState',
        [
          'BUILDING',
          'CANCELLED',
          'CONFIGURING',
          'DEPLOYING',
          'FAILED',
          'NOTFOUND',
          'RUNNING',
          'SUBMITTED',
          'DELETE_SUBMITTED',
          'DELETE_REQUEUE',
          'DELETE_DONE',
        ],
        'SUBMITTED'
      )}
      i18nError={errorText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
    />
  ))
  .add('unpublish submitted state', () => (
    <VirtualizationPublishStatus
      currentState={select(
        'currentState',
        [
          'BUILDING',
          'CANCELLED',
          'CONFIGURING',
          'DEPLOYING',
          'FAILED',
          'NOTFOUND',
          'RUNNING',
          'SUBMITTED',
          'DELETE_SUBMITTED',
          'DELETE_REQUEUE',
          'DELETE_DONE',
        ],
        'DELETE_SUBMITTED'
      )}
      i18nError={errorText}
      i18nPublishInProgress={publishInProgressText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
    />
  ));
