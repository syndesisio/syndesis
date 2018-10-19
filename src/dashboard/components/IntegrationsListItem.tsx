import {
  Label,
  ListView,
} from 'patternfly-react';
import * as React from 'react';
import {IIntegration} from "../../containers";

export interface IIntegrationsListItemProps {
  integration: IIntegration;
}


export class IntegrationsListItem extends React.Component<IIntegrationsListItemProps> {
  public render() {
    return (
      <ListView.Item
        actions={<div/>}
        additionalInfo={[
          <ListView.InfoItem key={1}>
            <Label>{this.props.integration.currentState}</Label>
          </ListView.InfoItem>,
        ]}
        heading={this.props.integration.name}
        hideCloseIcon={true}
        leftContent={<ListView.Icon name={'gear'} />}
        stacked={true}
      />
    )
  }
}