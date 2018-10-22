import { DropdownKebab, Icon, ListView, MenuItem } from 'patternfly-react';
import * as React from 'react';
import { IMonitoredIntegration } from '../containers';
import { IntegrationStatus } from './IntegrationStatus';
import { IntegrationStatusDetail } from './IntegrationStatusDetail';

export interface IIntegrationsListItemProps {
  monitoredIntegration: IMonitoredIntegration;
}

export class IntegrationsListItem extends React.Component<IIntegrationsListItemProps> {
  public render() {
    const {integration, monitoring} = this.props.monitoredIntegration;
    return (
      <ListView.Item
        actions={<div>
          {integration.currentState === 'Pending'
            ? <IntegrationStatusDetail
              targetState={integration.targetState}
              monitoring={monitoring}
            />
            : <IntegrationStatus integration={integration}/>
          }
          <DropdownKebab
            id={`integration-${integration.id}-action-menu`}
            pullRight={true}
          >
            <MenuItem>Action 2</MenuItem>
          </DropdownKebab>
        </div>}
        heading={integration.name}
        description={
          integration.board.warnings || integration.board.errors || integration.board.notices
            ? <>
              <Icon type={'pf'} name={'warning-triangle-o'}/>
              Configuration Required
            </>
            : ''
        }
        hideCloseIcon={true}
        leftContent={<ListView.Icon name={'gear'}/>}
        stacked={false}
      />
    )
  }
}