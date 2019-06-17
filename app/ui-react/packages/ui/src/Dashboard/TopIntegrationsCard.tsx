import * as H from '@syndesis/history';
import { Card } from 'patternfly-react';
import * as React from 'react';
import { IntegrationsEmptyState } from '../Integration';

import './TopIntegrations.css';

export interface ITopIntegrationsProps {
  i18nCreateIntegration: string;
  i18nCreateIntegrationTip?: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLast30Days: string;
  i18nTitle: string;
  linkCreateIntegration: H.LocationDescriptor;
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
        <Card.Body>
          {this.props.children ? (
            this.props.children
          ) : (
            <IntegrationsEmptyState
              i18nCreateIntegration={this.props.i18nCreateIntegration}
              i18nCreateIntegrationTip={this.props.i18nCreateIntegrationTip}
              i18nEmptyStateInfo={this.props.i18nEmptyStateInfo}
              i18nEmptyStateTitle={this.props.i18nEmptyStateTitle}
              linkCreateIntegration={this.props.linkCreateIntegration}
            />
          )}
        </Card.Body>
      </Card>
    );
  }
}
