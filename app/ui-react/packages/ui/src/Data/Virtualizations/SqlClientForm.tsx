import {
  Card,
  CardBody,
  Form,
  Stack,
  StackItem,
} from '@patternfly/react-core';
import { Button } from "patternfly-react";
import * as React from 'react';

export interface ISqlClientFormProps {
  /**
   * Localized text of the submit button.
   */
  i18nSubmit: string;

  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;
}

/**
 * A component to render the SqlClient entry form, to be used on the Virtualization SQL client page.
 * This does *not* build the form itself, form fields should be passed as the `children` value.
 */
export const SqlClientForm: React.FunctionComponent<
  ISqlClientFormProps
> = props => {
  return (
    <Stack>
      <StackItem isFilled={false}>
        <Card>
          <CardBody>
            <Form isHorizontal={true} onSubmit={props.handleSubmit}>
              {props.children}
            </Form>
          </CardBody>
        </Card>
      </StackItem>
      <StackItem isFilled={false}>
        <Button
          data-testid={'sql-client-form-submit-button'}
          bsStyle="primary"
          style={{ marginTop: 5 }}
          onClick={props.handleSubmit}
        >
          {props.i18nSubmit}
        </Button>
      </StackItem>
    </Stack>
  );
};
