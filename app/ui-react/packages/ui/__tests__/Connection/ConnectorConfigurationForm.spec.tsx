import * as React from 'react';
import '@testing-library/jest-dom/extend-expect';
import { cleanup, render } from '@testing-library/react';
import { ConnectorConfigurationForm } from '../../src/Connection';
import { MemoryRouter } from 'react-router';

export default describe('ConnectorConfigurationForm', () => {
  const props = {
    i18nFormTitle: '',
    i18nValidate: 'Validate',
    handleSubmit: jest.fn(),
    onValidate: jest.fn(),
    isNextDisabled: false,
    isNextLoading: false,
    isValidating: false,
  };

  afterEach(cleanup);

  it('should display properly', () => {
    const { getByTestId } = render(
      <MemoryRouter>
        <ConnectorConfigurationForm
          {...props}
          isNextLoading={false}
        />
      </MemoryRouter>
    );

    /**
     * Check that the form loads
     */
    expect(getByTestId('connector-configuration-form')).toBeVisible();
  });
});
