import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ExpandablePreview } from '../../../src';

const stories = storiesOf('Data/ViewEditor/ExpandablePreview', module);

stories.add('collapsed', () => {
  return (
    <ExpandablePreview
      i18nHidePreview={'Hide Preview'}
      i18nShowPreview={'Show Preview'}
      initialExpanded={false}
      onPreviewExpandedChanged={action('expanded changed')}
    />
  );
})

.add('expanded', () => {
  return (
    <ExpandablePreview
      i18nHidePreview={'Hide Preview'}
      i18nShowPreview={'Show Preview'}
      initialExpanded={true}
      onPreviewExpandedChanged={action('expanded changed')}
    />
  );
});
