import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ExtensionIntegrationsTable } from '../../src';

const stories = storiesOf(
  'Customization/Extensions/Component/ExtensionIntegrationsTable',
  module
);

const storyNotes = '- Verify that the used by message makes sense with the number provided.\n';

interface IExtensionIntegration {
  id: string; // used to create link to integration details page
  name: string;
  description: string;
}

const integration: IExtensionIntegration[] = [
  {
    id: 'i-M0boaSYifqPRAYunW2wz',
    name: 'OpenShift Log Integration',
    description: 'This is an integration that uses the OpenShift connection.'
  }
];

const integrations: IExtensionIntegration[] = [
  {
    id: 'i-M0boaSYifqPRAYunW2wz',
    name: 'OpenShift Log Integration',
    description: 'This is an integration that uses the OpenShift connection.'
  },
  {
    id: 'i-Lty73VPnQFjzDoL_Hzlz',
    name: 'FHIR Timer Integration',
    description: 'A deeply nested FHIR integration.'
  },
  {
    id: 'i-LwOwlxUU-y_8EuXFwsMz',
    name: 'Webhook to PostgresDB',
    description: 'This integration uses the Webhook and PostgresDB connections.'
  }
];

const usedByMessage0 = 'Used by 0 integrations';
const usedByMessage1 = 'Used by 1 integration';
const usedByMessage2 = 'Used by 2 integrations';

stories.add(
  '0 Integrations',
  () => (
    <ExtensionIntegrationsTable
      i18nDescription={text('Description', undefined)}
      i18nName={text('Name', undefined)}
      i18nUsageMessage={usedByMessage0}
      onSelectIntegration={action('select')}
      data={[]}
    />
  ),
  { notes: storyNotes }
);

stories.add(
  '1 Integration',
  () => (
    <ExtensionIntegrationsTable
      i18nDescription={text('Description', undefined)}
      i18nName={text('Name', undefined)}
      i18nUsageMessage={usedByMessage1}
      onSelectIntegration={action('select')}
      data={integration}
    />
  ),
  { notes: storyNotes }
);

stories.add(
  '2 Integrations',
  () => (
    <ExtensionIntegrationsTable
      i18nDescription={text('Description', undefined)}
      i18nName={text('Name', undefined)}
      i18nUsageMessage={usedByMessage2}
      onSelectIntegration={action('select')}
      data={integrations}
    />
  ),
  { notes: storyNotes }
);

