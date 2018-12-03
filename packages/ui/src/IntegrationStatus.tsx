import { Label } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationStatusProps {
  currentState?: string;
  i18nPublished: string;
  i18nUnpublished: string;
}

export class IntegrationStatus extends React.Component<
  IIntegrationStatusProps
> {
  public render() {
    const labelType =
      this.props.currentState === 'Published' ||
      this.props.currentState === 'Pending'
        ? 'primary'
        : 'default';
    let label = 'Pending';
    switch (this.props.currentState) {
      case 'Published':
        label = this.props.i18nPublished;
        break;
      case 'Unpublished':
        label = this.props.i18nUnpublished;
        break;
    }
    return <Label type={labelType}>{label}</Label>;
  }
}
