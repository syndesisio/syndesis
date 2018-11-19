import * as React from 'react';
import { Link } from 'react-router-dom';
import { IListViewToolbarProps, ListViewToolbar } from './ListViewToolbar';

export interface IIntegrationsListViewProps extends IListViewToolbarProps {
  linkToIntegrationImport: string;
  linkToIntegrationCreation: string;
}

export class IntegrationsListView extends React.Component<
  IIntegrationsListViewProps
> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <Link
              to={this.props.linkToIntegrationImport}
              className={'btn btn-default'}
            >
              Import
            </Link>
            <Link
              to={this.props.linkToIntegrationCreation}
              className={'btn btn-primary'}
            >
              Create Integration
            </Link>
          </div>
        </ListViewToolbar>
        <div className={'container-fluid'}>{this.props.children}</div>
      </>
    );
  }
}
