import { Radio } from '@patternfly/react-core';
import { Card, FormGroup } from 'patternfly-react';
import * as React from 'react';

export interface IApiClientConnectorCreateSecurityProps {
  /**
   * Required in OpenAPI 2.0 for the security scheme object.
   * The type of the security scheme. Valid values are "basic", "apiKey" or "oauth2".
   */
  authenticationType?: 'basic' | 'apiKey' | 'oauth2';
  /**
   * Locale string for when no security is specified
   */
  i18nNoSecurity: string;
  i18nTitle: string;
}

export class ApiClientConnectorCreateSecurity extends React.Component<
  IApiClientConnectorCreateSecurityProps
> {
  public render() {
    return (
      <Card style={{ maxWidth: '600px' }}>
        <Card.Heading>
          <Card.Title>{this.props.i18nTitle}</Card.Title>
        </Card.Heading>
        <Card.Body>
          <FormGroup controlId={'authenticationType'} disabled={false}>
            <Radio
              id={'authenticationType'}
              aria-label={'Authentication Type'}
              label={this.props.authenticationType || this.props.i18nNoSecurity}
              checked={true}
              name={'authenticationType'}
              readOnly={true}
            />
          </FormGroup>
        </Card.Body>
      </Card>
    );
  }
}
