import { storiesOf } from '@storybook/react';
import * as React from 'react';

const stories = storiesOf('Integration/Editor/IntegrationEditorLabels', module);

import { IntegrationEditorLabels } from '../../../src/Integration/Editor/IntegrationEditorLabels';

const labels = {
  key1: 'value1',
  key2: 'value2',
};

stories.add('Integration Editor Labels', () => (
  <IntegrationEditorLabels labels={labels} />
));
