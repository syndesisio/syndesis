import { Title } from '@patternfly/react-core';
import { Icon } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailEditableNameProps {
  name?: string;
}

export class IntegrationDetailEditableName extends React.PureComponent<
  IIntegrationDetailEditableNameProps
> {
  public render() {
    return (
      <>
        {this.props.name ? (
          <>
            <Title size="lg">{this.props.name}&nbsp;</Title>
            <Icon name={'pencil'} />
          </>
        ) : null}
      </>
    );
  }
}
