import { render } from '@testing-library/react';
import * as React from 'react';
import { ApiConnectorDetailBody } from '../../src/Customization/apiClientConnectors';

it('Renders the expected connector name and description', () => {
  const expectedIcon = 'some-icon';
  const expectedName = 'Test';
  const expectedDescription = 'This is a description.';
  const onSubmit = jest.fn();

  const { getByText, queryByTestId } = render(
    <ApiConnectorDetailBody
      description={expectedDescription}
      handleSubmit={onSubmit}
      i18nCancelLabel={'Cancel'}
      i18nEditLabel={'Edit'}
      i18nLabelBaseUrl={'Base URL'}
      i18nLabelDescription={'Description'}
      i18nLabelHost={'Host'}
      i18nLabelName={'Name'}
      i18nNameHelper={'Please provide a name for the API Connector'}
      i18nRequiredText={'The fields marked with * are required.'}
      i18nSaveLabel={'Save'}
      i18nTitle={expectedName + ' Configuration'}
      icon={expectedIcon}
      name={expectedName}
    />
  );

  // Expect the connector name to be visible
  expect(getByText(expectedName)).toBeInTheDocument();

  // Expect the connector description to be visible
  expect(getByText(expectedDescription)).toBeInTheDocument();

  // check that data-testids contain expected content
  expect(queryByTestId('api-connector-detail-body')).toHaveTextContent(
    expectedName
  );

  expect(queryByTestId('api-connector-detail-body')).toHaveTextContent(
    expectedDescription
  );
});
