import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { IntegrationsPage } from '../src/modules/integrations/pages';

const stories = storiesOf('Integration', module);

stories.add('Integrations list page', () => <IntegrationsPage />);
