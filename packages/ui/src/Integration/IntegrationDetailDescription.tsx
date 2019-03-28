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
          <p>
            {this.props.description}&nbsp;
            <Icon name={'pencil'} />
          </p>
        ) : null}
      </div>
    );
  }
}
