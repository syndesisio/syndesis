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
export class SqlClientForm extends React.Component<ISqlClientFormProps> {
  public render() {
    return (
      <>
        <Container>
          <form
            className="form-horizontal required-pf"
            role="form"
            onSubmit={this.props.handleSubmit}
          >
            <div className="row row-cards-pf">
              <div className="card-pf">
                <div className="card-pf-body">
                  <Container>{this.props.children}</Container>
                </div>
              </div>
            </div>
          </form>
          <button
            data-testid={'sql-client-form-submit-button'}
            type="button"
            className="btn btn-primary"
            onClick={this.props.handleSubmit}
          >
            Submit
          </button>
        </Container>
      </>
    );
  }
}
