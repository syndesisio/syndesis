import * as React from 'react';

export class IntegrationStepsHorizontalView extends React.Component {
  public render() {
    return <div className="show-grid">{this.props.children}</div>;
  }
}
