import * as React from 'react';

export interface IFormWrapperProps {
  onSubmit: (event: React.FormEvent<HTMLFormElement>) => void;
  fields: JSX.Element;
}

export class FormWrapper extends React.Component<IFormWrapperProps> {
  public render() {
    return (
      <form onSubmit={this.props.onSubmit}>
        <div className="container-fluid">
          <p className="fields-status-pf">
            The fields marked with <span className="required-pf">*</span> are
            required.
          </p>
          {this.props.fields}
        </div>
        <button type="submit" className="btn btn-primary">
          Submit
        </button>
      </form>
    );
  }
}
