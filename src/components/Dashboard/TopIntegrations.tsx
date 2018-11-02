import { IntegrationsList, IntegrationsListSkeleton } from '@syndesis/ui/components';
import { IMonitoredIntegration } from '@syndesis/ui/containers';
import { Card, MenuItem } from 'patternfly-react';
import * as React from 'react';

import './TopIntegrations.css';

export interface ITopIntegrationsProps {
  loading: boolean;
  topIntegrations: IMonitoredIntegration[]
}

export class TopIntegrations extends React.Component<ITopIntegrationsProps> {
  public render() {
    return (
      <Card accented={false} className={'TopIntegrations'}>
        <Card.Heading>
          <Card.DropdownButton id='cardDropdownButton1' title='Last 30 Days'>
            <MenuItem eventKey='1' active={true}>
              Last 30 Days
            </MenuItem>
            <MenuItem eventKey='2'>
              Last 60 Days
            </MenuItem>
            <MenuItem eventKey='3'>
              Last 90 Days
            </MenuItem>
          </Card.DropdownButton>
          <Card.Title>
            Top 5 Integrations
          </Card.Title>
        </Card.Heading>
        <Card.Body>
          {this.props.loading
            ? <IntegrationsListSkeleton width={500}/>
            : <IntegrationsList monitoredIntegrations={this.props.topIntegrations}/>
          }
        </Card.Body>
      </Card>
    );
  }
}