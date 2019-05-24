import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { SimplePageHeader } from '../../src';

const stories = storiesOf('Shared/SimplePageHeader', module);
stories.addDecorator(withKnobs);

stories.add('Normal', () => (
  <SimplePageHeader
    i18nTitle={text('Title', 'My Awesome page')}
    i18nDescription={text(
      'Description',
      'This description is even better than the title.'
    )}
  />
));
