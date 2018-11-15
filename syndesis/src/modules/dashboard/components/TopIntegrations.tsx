import { IntegrationWithMonitoring } from '@syndesis/models';
import {
  IntegrationsList,
  IntegrationsListItem,
  IntegrationsListSkeleton,
} from '@syndesis/ui';
import { Card, MenuItem } from 'patternfly-react';
import * as React from 'react';

import './TopIntegrations.css';

export interface ITopIntegrationsProps {
  loading: boolean;
  topIntegrations: IntegrationWithMonitoring[];
}

export class TopIntegrations extends React.Component<ITopIntegrationsProps> {
  public render() {
    return (
      <Card accented={false} className={'TopIntegrations'}>
        <Card.Heading>
          <Card.DropdownButton id="cardDropdownButton1" title="Last 30 Days">
            <MenuItem eventKey="1" active={true}>
              Last 30 Days
            </MenuItem>
            <MenuItem eventKey="2">Last 60 Days</MenuItem>
            <MenuItem eventKey="3">Last 90 Days</MenuItem>
          </Card.DropdownButton>
          <Card.Title>Top 5 Integrations</Card.Title>
        </Card.Heading>
        <Card.Body>
          {this.props.loading ? (
            <IntegrationsListSkeleton width={500} />
          ) : (
            <IntegrationsList>
              {this.props.topIntegrations.map(
                (mi: IntegrationWithMonitoring, index) => (
                  <IntegrationsListItem
                    integrationId={mi.integration.id!}
                    integrationName={mi.integration.name}
                    currentState={mi.integration!.currentState!}
                    targetState={mi.integration!.targetState!}
                    isConfigurationRequired={
                      !!(
                        mi.integration!.board!.warnings ||
                        mi.integration!.board!.errors ||
                        mi.integration!.board!.notices
                      )
                    }
                    monitoringValue={
                      mi.monitoring && mi.monitoring.detailedState.value
                    }
                    monitoringCurrentStep={
                      mi.monitoring && mi.monitoring.detailedState.currentStep
                    }
                    monitoringTotalSteps={
                      mi.monitoring && mi.monitoring.detailedState.totalSteps
                    }
                    key={index}
                  />
                )
              )}
            </IntegrationsList>
          )}
        </Card.Body>
      </Card>
    );
  }
}
