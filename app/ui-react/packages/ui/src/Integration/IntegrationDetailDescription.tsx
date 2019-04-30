import { Text } from '@patternfly/react-core';
import { Icon } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailDescriptionProps {
  description?: string;
  i18nNoDescription?: string;
}

export class IntegrationDetailDescription extends React.PureComponent<
  IIntegrationDetailDescriptionProps
> {
  public render() {
    return (
      <div>
        <Text>
          {this.props.description
            ? this.props.description
            : this.props.i18nNoDescription}
          &nbsp;
          <Icon name={'pencil'} />
        </Text>
      </div>
    );
  }
}
