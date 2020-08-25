import { render } from '@testing-library/react';
import * as React from 'react';
import { ApiConnectorDetailConfigEdit } from '../../src/Customization/apiClientConnectors';

it('Renders the component, its properties, and data-testids', () => {
  const expectedIcon = 'some-icon';
  const expectedName = 'Test';
  const expectedDescription = 'This is a description.';
  const handleOnChange = jest.fn();

  const properties = {
    description: expectedDescription,
    icon: expectedIcon,
    name: expectedName,
  };

  const { queryByTestId } = render(
    <ApiConnectorDetailConfigEdit
      handleOnChange={handleOnChange}
      i18nLabelBaseUrl={'Base URL'}
      i18nLabelDescription={'Description'}
      i18nLabelHost={'Host'}
      i18nLabelName={'Name'}
      i18nNameHelper={'Please provide a name for the API Connector'}
      i18nRequiredText={'The fields marked with * are required.'}
      properties={properties}
    />
  );

  // check presence of data-testids
  expect(queryByTestId('api-connector-details-form')).toBeInTheDocument();
  expect(queryByTestId('api-connector-name-field')).toBeInTheDocument();
  expect(queryByTestId('api-connector-details-form')).toBeInTheDocument();
});
