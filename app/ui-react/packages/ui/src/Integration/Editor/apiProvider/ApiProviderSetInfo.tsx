import * as React from 'react';
import { Container } from '../../../Layout';

export interface IApiProviderSetInfoProps {
  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;
}

export class ApiProviderSetInfo extends React.Component<
  IApiProviderSetInfoProps
> {
  public render() {
    return (
      <Container>
        <form
          className={'required-pf'}
          role={'form'}
          onSubmit={this.props.handleSubmit}
        >
          <div className="row row-cards-pf">
            <div className="card-pf">
              <div className="card-pf-body">{this.props.children}</div>
            </div>
          </div>
        </form>
      </Container>
    );
  }
}
