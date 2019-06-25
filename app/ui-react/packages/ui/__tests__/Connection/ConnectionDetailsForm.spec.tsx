import * as React from 'react';
import 'jest-dom/extend-expect';
import { cleanup, render } from 'react-testing-library';
import {
  ConnectionDetailsForm,
  IConnectionDetailsValidationResult,
} from '../../src/Connection';
import { MemoryRouter } from 'react-router';

export default describe('ConnectionDetailsForm', () => {
  const validationResults: IConnectionDetailsValidationResult[] = [
    { message: 'A success message', type: 'success' },
    { message: 'An error message', type: 'error' },
  ];

  const props = {
    handleSubmit: jest.fn(),
    i18nCancelLabel: 'Cancel',
    i18nEditLabel: 'Edit',
    i18nSaveLabel: 'Save',
    i18nTitle: 'Connection Details Form',
    i18nValidateLabel: 'Validate',
    onCancelEditing: jest.fn(),
    onStartEditing: jest.fn(),
    onValidate: jest.fn(),
    validationResults: validationResults,
  };

  afterEach(cleanup);

  it('should display properly when the form is valid and not being edited', () => {
    const { getByTestId } = render(
      <MemoryRouter>
        <ConnectionDetailsForm
          {...props}
          isEditing={false}
          isValid={true}
          isWorking={false}
        />
      </MemoryRouter>
    );

    /**
     * Ensure the form itself is rendered.
     */
    expect(getByTestId('connection-details-form')).toBeVisible();

    /**
     * Ensure the Edit button is displayed.
     */
    expect(getByTestId('connection-details-form-edit-button')).toBeVisible();
  });

  it('should display the validate button if the form is being edited', () => {
    const { getByTestId } = render(
      <MemoryRouter>
        <ConnectionDetailsForm
          {...props}
          isEditing={true}
          isValid={true}
          isWorking={false}
        />
      </MemoryRouter>
    );

    /**
     * Ensure the Validate button is displayed.
     */
    expect(
      getByTestId('connection-details-form-validate-button')
    ).toBeVisible();

    /**
     * Ensure the Cancel button is displayed.
     */
    expect(getByTestId('connection-details-form-cancel-button')).toBeVisible();

    /**
     * Ensure the Save button is displayed.
     */
    expect(getByTestId('connection-details-form-save-button')).toBeVisible();
  });
});
