import * as React from 'react';
import 'jest-dom/extend-expect';
import { cleanup, render } from 'react-testing-library';
import { ConnectorConfigurationForm } from '../../src/Connection';
import { MemoryRouter } from 'react-router';

export default describe('ConnectorConfigurationForm', () => {
  const props = {
    i18nFormTitle: '',
    handleSubmit: jest.fn(),
    onNext: jest.fn(),
    onValidate: jest.fn(),
    backHref: '/',
    isNextDisabled: false,
    isNextLoading: false,
    isValidating: false,
    isLastStep: false,
    i18nSave: 'Save',
    i18nNext: 'Next',
  };

  afterEach(cleanup);

  it('should display properly', () => {
    const { getByTestId } = render(
      <MemoryRouter>
        <ConnectorConfigurationForm
          {...props}
          isNextLoading={false}
          isLastStep={false}
        />
      </MemoryRouter>
    );

    /**
     * Check that the form loads
     */
    expect(getByTestId('connector-configuration-form')).toBeVisible();

    /**
     * Next button should always be visible,
     * but the text will be different based on whether or not Next is loading,
     * and if it's the last step it should say Save instead of Next
     */
    const nextButton = getByTestId('connection-creator-layout-next-button');
    expect(nextButton).toBeVisible();
    expect(nextButton).toHaveTextContent(props.i18nNext);
  });

  it('should display Save instead of Next on the last step', () => {
    const { getByTestId } = render(
      <MemoryRouter>
        <ConnectorConfigurationForm
          {...props}
          isNextLoading={false}
          isLastStep={true}
        />
      </MemoryRouter>
    );

    /**
     * Check that the form loads
     */
    expect(getByTestId('connector-configuration-form')).toBeVisible();

    /**
     * Next button should always be visible,
     * but the text will be different based on whether or not Next is loading,
     * and if it's the last step it should say Save instead of Next
     */
    const nextButton = getByTestId('connection-creator-layout-next-button');
    expect(nextButton).toBeVisible();
    expect(nextButton).toHaveTextContent(props.i18nSave);
  });
});
