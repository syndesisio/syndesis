import { Label } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationStatusProps {
  currentState: string;
}

export class IntegrationStatus extends React.Component<IIntegrationStatusProps> {
  public render() {
    const labelType =
      this.props.currentState === 'Published' || this.props.currentState === 'Pending'
        ? 'primary'
        : 'default';
    let label = 'Pending';
    switch (this.props.currentState) {
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