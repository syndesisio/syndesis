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
          <p>
            {this.props.name}&nbsp;
            <Icon name={'pencil'} />
          </p>
        ) : null}
      </>
    );
  }
}
