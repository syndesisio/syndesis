import { Alert, Card, CardBody, Form } from '@patternfly/react-core';
import * as React from 'react';

export interface IViewConfigurationFormValidationResult {
  message: string;
  type: 'danger' | 'success';
}

export interface IViewConfigurationFormProps {
  /**
   * Form level validationResults
   */
  validationResults: IViewConfigurationFormValidationResult[];
  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;
}

/**
 * A component to render a save form, to be used in the create view wizard.
 * This does *not* build the form itself, form's field should be passed
 * as the `children` value.
 */
export const ViewConfigurationForm: React.FunctionComponent<
IViewConfigurationFormProps
> = props => {
  return (
    <Card>
      <CardBody>
        <Form
          isHorizontal={true}
          data-testid={'view-configuration-form'}
          onSubmit={props.handleSubmit}
        >
          {props.validationResults.map((e, idx) => (
            <Alert key={idx} title={''} variant={e.type}>
              {e.message}
            </Alert>
          ))}
          {props.children}
        </Form>
      </CardBody>
    </Card>
  );
}
