import { Card, MenuItem } from 'patternfly-react';
import * as React from 'react';
import { IMonitoredIntegration } from '../../containers';
import { IntegrationsList } from '../index';

export interface ITopIntegrationsProps {
  integrations: IMonitoredIntegration[]
}

export class TopIntegrations extends React.Component<ITopIntegrationsProps> {
  public render() {
    return (
      <Card accented={false}>
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
          <IntegrationsList monitoredIntegrations={this.props.integrations}/>
        </Card.Body>
      </Card>
    );
  }
}