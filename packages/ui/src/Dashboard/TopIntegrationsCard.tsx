import { Card, MenuItem } from 'patternfly-react';
import * as React from 'react';

import './TopIntegrations.css';

export interface ITopIntegrationsProps {
  i18nLast30Days: string;
  i18nLast60Days: string;
  i18nLast90Days: string;
  i18nTitle: string;
}

export class TopIntegrationsCard extends React.Component<
  ITopIntegrationsProps
> {
  public render() {
    return (
      <Card accented={false} className={'TopIntegrationsCard'}>
        <Card.Heading>
          <Card.DropdownButton
            id="cardDropdownButton1"
            title={this.props.i18nLast30Days}
          >
            <MenuItem eventKey="1" active={true}>
              {this.props.i18nLast30Days}
            </MenuItem>
            <MenuItem eventKey="2">{this.props.i18nLast60Days}</MenuItem>
            <MenuItem eventKey="3">{this.props.i18nLast90Days}</MenuItem>
          </Card.DropdownButton>
          <Card.Title>{this.props.i18nTitle}</Card.Title>
        </Card.Heading>
        <Card.Body>{this.props.children}</Card.Body>
      </Card>
    );
  }
}
