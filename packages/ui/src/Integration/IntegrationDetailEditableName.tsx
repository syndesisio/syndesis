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
            <h1>{this.props.name}&nbsp;</h1>
            <Icon name={'pencil'} />
          </>
        ) : null}
      </>
    );
  }
}
