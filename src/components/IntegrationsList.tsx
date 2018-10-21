import { ListView, } from 'patternfly-react';
import * as React from 'react';
import { IIntegration } from '../containers';
import { IntegrationsListItem } from './IntegrationsListItem';

export interface IIntegrationsListProps {
  integrations: IIntegration[];
}


export class IntegrationsList extends React.Component<IIntegrationsListProps> {
  public render() {
    return (
      <ListView>
        {this.props.integrations.map((integration: IIntegration, index) => (
          <IntegrationsListItem
            integration={integration}
            key={index}
          />
        ))}
      </ListView>
    )
  }
}