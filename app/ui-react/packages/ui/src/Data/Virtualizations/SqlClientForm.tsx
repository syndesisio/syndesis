import { Form } from '@patternfly/react-core';
import * as React from 'react';
import { Container } from '../../Layout';

export interface ISqlClientFormProps {
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
    <>
      <Container>
        <form
          className="form-horizontal required-pf"
          role="form"
          onSubmit={props.handleSubmit}
        >
          <div className="row row-cards-pf">
            <div className="card-pf">
              <div className="card-pf-body">
                <Container>
                  <Form isHorizontal={true} onSubmit={props.handleSubmit}>
                    {props.children}
                  </Form>
                </Container>
              </div>
            </div>
          </div>
        </form>
        <button
          data-testid={'sql-client-form-submit-button'}
          type="button"
          className="btn btn-primary"
          onClick={props.handleSubmit}
        >
          Submit
          </button>
      </Container>
    </>
  );
}
