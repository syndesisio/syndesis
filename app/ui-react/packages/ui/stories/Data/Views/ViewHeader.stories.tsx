import { text } from '@storybook/addon-knobs';
import { withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ViewHeader } from '../../../src';

const stories = storiesOf('Data/Virtualizations/Views/ViewHeader', module);
stories.addDecorator(withKnobs);

const title = 'MyVirt';
const description = 'Description for MyVirt';

stories.add('Sample', () => (
  <ViewHeader
    i18nTitle={text('title', title)}
    i18nDescription={text('description', description)}
  />
));
