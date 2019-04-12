import { Text } from '@patternfly/react-core';
import { Icon } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailDescriptionProps {
  description?: string;
}

export class IntegrationDetailDescription extends React.PureComponent<
  IIntegrationDetailDescriptionProps
> {
  public render() {
    return (
      <div>
        {this.props.description ? (
          <Text>
            {this.props.description}&nbsp;
            <Icon name={'pencil'} />
          </Text>
        ) : null}
      </div>
    );
  }
}
