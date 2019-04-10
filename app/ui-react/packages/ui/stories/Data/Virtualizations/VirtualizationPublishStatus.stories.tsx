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
        ],
        'RUNNING'
      )}
      i18nError={errorText}
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
        ],
        'NOTFOUND'
      )}
      i18nError={errorText}
      i18nPublished={publishedText}
      i18nUnpublished={unpublishedText}
    />
  ));
