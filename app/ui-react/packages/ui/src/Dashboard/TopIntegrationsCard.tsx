import { Split, SplitItem } from '@patternfly/react-core';
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
        <Card.Heading>
          <Split>
            <SplitItem isFilled={true}>
              <Card.Title>{this.props.i18nTitle}</Card.Title>
            </SplitItem>
            <SplitItem isFilled={false} className={'heading__right'}>
              {this.props.i18nLast30Days}
            </SplitItem>
          </Split>
        </Card.Heading>
        <Card.Body>{this.props.children}</Card.Body>
      </Card>
    );
  }
}
