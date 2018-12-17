import * as React from 'react';
import { Link } from 'react-router-dom';
import { IListViewToolbarProps, ListViewToolbar } from '../Shared';

export interface IIntegrationsListViewProps extends IListViewToolbarProps {
  linkToIntegrationImport: string;
  linkToIntegrationCreation: string;
  i18nImport: string;
  i18nLinkCreateConnection: string;
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
              {this.props.i18nImport}
            </Link>
            <Link
              to={this.props.linkToIntegrationCreation}
              className={'btn btn-primary'}
            >
              {this.props.i18nLinkCreateConnection}
            </Link>
          </div>
        </ListViewToolbar>
        <div className={'container-fluid'}>{this.props.children}</div>
      </>
    );
  }
}
