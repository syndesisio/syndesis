import { storiesOf } from '@storybook/react';
import * as React from 'react';
const stories = storiesOf('Integration/Editor/IntegrationEditorExtensionList', module);

import {
  IntegrationEditorExtensionList
} from '../../../src';
import { action } from "@storybook/addon-actions";
import { text } from "@storybook/addon-knobs";

const extensions = {
  "items": [
    {
      "createdDate": 1585137219989,
      "description": "Syndesis Extension for adding a custom JDBC Driver",
      "extensionId": "io.syndesis.extensions:syndesis-library-test-driver",
      "extensionType": "Libraries",
      "icon": "data:image/svg+xml,%3Csvg[...]",
      "id": "i-M3GXbaK42YWhSEEgEVRz",
      "lastUpdated": 1585137224076,
      "name": "Example JDBC Driver Library",
      "schemaVersion": "v1",
      "status": "Installed",
      "tags": [
        "jdbc-driver"
      ],
      "userId": "developer",
      "uses": 0,
      "version": "1.0.0"
    },
    {
      "createdDate": 1583157229982,
      "description": "This is another example.",
      "extensionId": "io.syndesis.extensions:syndesis-library-example-two",
      "extensionType": "Libraries",
      "icon": "data:image/svg+xml,%3Csvg[...]",
      "id": "i-A2GXbaK42YWhSzZFE2md",
      "lastUpdated": 1585137224076,
      "name": "Example Two",
      "schemaVersion": "v1",
      "status": "Installed",
      "tags": [
        "example-two"
      ],
      "userId": "developer",
      "uses": 0,
      "version": "1.0.0"
    }
  ],
  "totalCount": 2
};

const selectedExtensionNames = ["io.syndesis.extensions:syndesis-library-test-driver"];

stories
.add('Integration Editor Extension List', () => (
  <IntegrationEditorExtensionList extensionsAvailable={extensions.items}
                                  extensionNamesSelected={selectedExtensionNames}
                                  handleSelectAll={action('handleSelectAll')}
                                  i18nHeaderDescription={text('Description', '?')}
                                  i18nHeaderLastUpdated={text('integrations:editor:extensions:lastUpdated', '')}
                                  i18nHeaderName={text('integrations:editor:extensions:name', '')}
                                  i18nTableDescription={text('integrations:editor:extensions:tableDescription', '')}
                                  i18nTableName={text('integrations:editor:extensions:tableName', '')}
                                  onSelect={action('Selected')}
  />
));
