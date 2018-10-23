import { Label } from 'patternfly-react';
import * as React from 'react';
import { IIntegration } from '../containers';

export interface IIntegrationStatusProps {
  integration: IIntegration;
}


export class IntegrationStatus extends React.Component<IIntegrationStatusProps> {
  public render() {
    const labelType =
      this.props.integration.currentState === 'Published' || this.props.integration.currentState === 'Pending'
        ? 'primary'
        : 'default';
    let label = 'Pending';
    switch (this.props.integration.currentState) {
      case 'Published':
        label = 'Published';
        break;
      case 'Unpublished':
        label = 'Unpublished';
        break;
    }
    return (
      <Label type={labelType}>{label}</Label>
    );
  }
}