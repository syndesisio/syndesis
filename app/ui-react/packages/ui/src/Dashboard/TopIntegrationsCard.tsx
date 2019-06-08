import { Card } from 'patternfly-react';
import * as React from 'react';

import './TopIntegrations.css';

export interface ITopIntegrationsProps {
  i18nLast30Days: string;
  i18nTitle: string;
}

export class TopIntegrationsCard extends React.Component<
  ITopIntegrationsProps
> {
  public render() {
    return (
      <Card accented={false} className={'top-integrations'}>
        <Card.Heading className={'top-integrations__heading'}>
          <Card.Title>{this.props.i18nTitle}</Card.Title>
          <div className={'top-integrations__heading-daterange'}>
            {this.props.i18nLast30Days}
          </div>
        </Card.Heading>
        <Card.Body>{this.props.children}</Card.Body>
      </Card>
    );
  }
}
