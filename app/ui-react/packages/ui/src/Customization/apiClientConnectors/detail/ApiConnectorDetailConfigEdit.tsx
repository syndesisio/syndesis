import { Form } from '@patternfly/react-core';
import * as React from 'react';

export interface IApiConnectorDetailConfigEdit {
  // Initial properties
  properties?: any;
}

export const ApiConnectorDetailConfigEdit: React.FunctionComponent<IApiConnectorDetailConfigEdit> = ({
  properties,
}) => {
  // tslint:disable:no-console

  return (
    <>
      <Form isHorizontal={true} data-testid={'api-connector-details-form'}>
        <p>Children go here..</p>
      </Form>
    </>
  );
};
