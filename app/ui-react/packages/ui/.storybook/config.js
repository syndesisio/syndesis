import '../../.storybook/config';
import { configure } from '@storybook/react';

/* load all stories in the story folder */
const req = require.context('../stories', true, /\.stories\.tsx$/);
function loadStories() {
  req.keys().forEach(filename => req(filename));
}
configure(loadStories, module);
