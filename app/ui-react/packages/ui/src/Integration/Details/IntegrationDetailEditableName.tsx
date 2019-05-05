import { Title } from '@patternfly/react-core';
import * as React from 'react';

export interface IIntegrationDetailEditableNameProps {
  name?: React.ReactNode;
}

export class IntegrationDetailEditableName extends React.PureComponent<
  IIntegrationDetailEditableNameProps
> {
  public render() {
    return <>{this.props.name && <Title size="lg">{this.props.name}</Title>}</>;
  }
}
