import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

const stories = storiesOf('Integration/Editor/IntegrationEditorLabels', module);

import { IntegrationEditorLabels } from '../../../src/Integration/Editor/IntegrationEditorLabels';

const labels = ['rachel=pizza', 'lex=hotdogs'];

stories.add('Integration Editor Labels', () => (
  <IntegrationEditorLabels
    initialLabels={labels}
    onSelectLabels={action('Selected')}
  />
));
