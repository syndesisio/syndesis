import { render } from '@testing-library/react';
import * as React from 'react';
import { ApiConnectorDetailHeader } from '../../src/Customization/apiClientConnectors';

it('Renders the expected connector name and description', () => {
  const expectedDescription = 'This is a description.';
  const expectedIcon = 'some-icon';
  const expectedName = 'Test';
  const expectedUsageMessage = 'Used by integrations 2 times';

  const { getByText } = render(
    <ApiConnectorDetailHeader
      connectorDescription={expectedDescription}
      i18nDescription={'Description'}
      i18nUsageLabel={'Usage'}
      i18nUsageMessage={expectedUsageMessage}
      connectorIcon={expectedIcon}
      connectorName={expectedName}
    />
  );

  // Expect the connector name to be visible
  expect(getByText(expectedName)).toBeInTheDocument();

  // Expect the connector description to be visible
  expect(getByText(expectedDescription)).toBeInTheDocument();
});
