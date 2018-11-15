import { addDecorator, configure } from '@storybook/react';
import { setOptions } from '@storybook/addon-options';
import { withInfo } from '@storybook/addon-info';
import { checkA11y } from '@storybook/addon-a11y';
import { withKnobs } from '@storybook/addon-knobs';
import * as React from 'react';

const req = require.context('../stories', true, /\.stories\.tsx$/);

function loadStories() {
  req.keys().forEach(filename => req(filename));
}

addDecorator(
  withInfo({
    inline: true,
    header: false,
    maxPropsIntoLine: 1,
  })
);
addDecorator(checkA11y);
addDecorator(withKnobs);
configure(loadStories, module);
