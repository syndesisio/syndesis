import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { IntegrationsEmptyState } from '../../src';

const stories = storiesOf('Integration/IntegrationsEmptyState', module);

const createTip = 'Click to begin creating a new integration';
const info =
  'There are currently no integrations. Click the button below to create one.';
const link = '/integrations/create/new-integration/start/abcdefg';
const title = 'Create Integration';

const storyNotes =
  '- Verify title is "' +
  title +
  '"\n' +
  '- Verify info is "' +
  info +
  '"\n' +
  '- Verify button text is "' +
  title +
  '"\n' +
  '- Verify button toolipt is "' +
  createTip +
  '"\n' +
  '- Verify clicking button prints "' +
  link +
  '" in the **Actions** tab';

stories.add(
  'render',
  () => (
    <Router>
      <IntegrationsEmptyState
        i18nCreateIntegration={title}
        i18nCreateIntegrationTip={createTip}
        i18nEmptyStateInfo={info}
        i18nEmptyStateTitle={title}
        linkCreateIntegration={action('blah')}
      />
    </Router>
  ),
  { notes: storyNotes }
);
