import {
  ActionGroup,
  Alert,
  Button,
  ButtonVariant,
  Card,
  CardBody,
  Form,
} from '@patternfly/react-core';
import * as React from 'react';
import './VirtualizationCreateForm.css';

export interface IVirtualizationCreateValidationResult {
  message: string;
  type: 'danger' | 'success';
}

export interface IVirtualizationCreateFormProps {
  /**
   * The localized text for the cancel button.
   */
  i18nCancelLabel: string;

  /**
   * The localized text for the create button.
   */
  i18nCreateLabel: string;

  /**
   * `true` if create should be disabled.
   */
  isDisableCreate: boolean;

  /**
   * `true` if work in progress and this form should disable user input.
   */
  isWorking: boolean;

  /**
   * Form level validationResults
   */
  validationResults: IVirtualizationCreateValidationResult[];

  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;

  /**
   * The callback for cancel.
   */
  onCancel: () => void;
}

export const VirtualizationCreateForm: React.FunctionComponent<
  IVirtualizationCreateFormProps
> = props => {
  return (
    <Card>
      <CardBody>
        <Form
          isHorizontal={true}
          data-testid={'virtualization-create-form'}
          onSubmit={props.handleSubmit}
        >
          {props.validationResults.map((e, idx) => (
            <Alert key={idx} title={''} variant={e.type}>
              {e.message}
            </Alert>
          ))}
          {props.children}
          <ActionGroup>
            <Button
              data-testid={'virtualization-create-form-save-button'}
              className={'virtualization-create-form__saveButton'}
              isDisabled={props.isWorking || props.isDisableCreate}
              variant={ButtonVariant.primary}
              onClick={props.handleSubmit}
            >
              {props.i18nCreateLabel}
            </Button>
            <Button
              data-testid={'virtualization-create-form-cancel-button'}
              className={'virtualization-create-form__cancelButton'}
              isDisabled={props.isWorking}
              variant={ButtonVariant.secondary}
              onClick={props.onCancel}
            >
              {props.i18nCancelLabel}
            </Button>
          </ActionGroup>
        </Form>
      </CardBody>
    </Card>
  );
};
