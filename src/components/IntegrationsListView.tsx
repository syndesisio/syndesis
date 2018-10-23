import * as React from 'react';
import { Link } from 'react-router-dom';
import { IMonitoredIntegration } from '../containers';
import { IntegrationsList } from './IntegrationsList';
import { IListViewToolbarProps, ListViewToolbar } from './ListViewToolbar';

export interface IIntegrationsListViewProps extends IListViewToolbarProps {
  match: any;
  monitoredIntegrations: IMonitoredIntegration[];
}

export class IntegrationsListView extends React.Component<IIntegrationsListViewProps> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <Link
              to={`${this.props.match.url}/import`}
              className={'btn btn-default'}
            >
              Import
            </Link>
            <Link
              to={`${this.props.match.url}/new`}
              className={'btn btn-primary'}
            >
              Create Integration
            </Link>
          </div>
        </ListViewToolbar>
        <div className={'container-fluid'}>
          <IntegrationsList monitoredIntegrations={this.props.monitoredIntegrations}/>
        </div>
      </>
    );
  }

}