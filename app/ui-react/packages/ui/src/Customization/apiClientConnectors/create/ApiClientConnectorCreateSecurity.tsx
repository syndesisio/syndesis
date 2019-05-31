import { Card } from 'patternfly-react';
import * as React from 'react';

export interface IApiClientConnectorCreateSecurityProps {
  i18nTitle?: string;
}

export class ApiClientConnectorCreateSecurity extends React.Component<
  IApiClientConnectorCreateSecurityProps
> {
  public render() {
    return (
      <Card>
        <Card.Heading>
          <Card.Title>{this.props.i18nTitle}</Card.Title>
        </Card.Heading>
        <Card.Body />
      </Card>
    );
  }
}
