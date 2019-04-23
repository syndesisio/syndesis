import { action } from '@storybook/addon-actions';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { TagIntegrationListItem } from '../../../src';

const stories = storiesOf('Integration/CiCd/TagIntegrationListItem', module);
stories.addDecorator(withKnobs);

stories.add('Normal', () => (
  <ListView>
    <TagIntegrationListItem
      selected={false}
      name={text('Name', 'Development')}
      onChange={action('onChange')}
    />
  </ListView>
));
