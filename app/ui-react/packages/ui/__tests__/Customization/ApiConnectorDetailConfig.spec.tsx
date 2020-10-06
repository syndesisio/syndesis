import { render } from '@testing-library/react';
import * as React from 'react';
import {
  ApiConnectorDetailBody,
  ApiConnectorDetailConfig,
} from '../../src/Customization/apiClientConnectors';

it('Renders the component, its properties, and data-testids', () => {
  const expectedBaseUrl = '/';
  const expectedHost = 'api.something.com';
  const expectedIcon = 'some-icon';
  const expectedName = 'Test';
  const expectedDescription = 'This is a description.';

  const properties = {
    basePath: expectedBaseUrl,
    description: expectedDescription,
    host: expectedHost,
    icon: expectedIcon,
    name: expectedName,
  };

  const { getByText, queryByTestId } = render(
    <ApiConnectorDetailConfig
      i18nLabelAddress={'Address'}
      i18nLabelBaseUrl={'Base URL'}
      i18nLabelDescription={'Description'}
      i18nLabelHost={'Host'}
      i18nLabelName={'Name'}
      properties={properties}
    />
  );

  // Expect the connector name to be visible
  expect(getByText(expectedName)).toBeInTheDocument();

  // Expect the connector description to be visible
  expect(getByText(expectedDescription)).toBeInTheDocument();

  // Expect the connector base URL to be visible
  expect(getByText(expectedBaseUrl)).toBeInTheDocument();

  // Expect the connector hosts to be visible
  expect(getByText(expectedHost)).toBeInTheDocument();

  // verify that data-testids are in the document
  expect(queryByTestId('api-connector-detail-config')).toBeInTheDocument();
});
