import { DataList } from '@patternfly/react-core';
import { action } from '@storybook/addon-actions';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { TagIntegrationListItem } from '../../../src';

const stories = storiesOf('Integration/CiCd/TagIntegrationListItem', module);
stories.addDecorator(withKnobs);

stories.add('Normal', () => (
  <DataList aria-label={'TagIntegrationList'}>
    <TagIntegrationListItem
      selected={false}
      name={text('Name', 'Development')}
      onChange={action('onChange')}
    />
  </DataList>
));
