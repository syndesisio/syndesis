import { ConnectionOverview } from '@syndesis/models';
import { PageHeader } from '@syndesis/ui';
import * as H from 'history';
import * as React from 'react';
import { ConnectionsWithToolbar } from '../../connections/containers';

export interface IIntegrationEditorChooseConnection {
  breadcrumb: JSX.Element;
  error: boolean;
  loading: boolean;
  connections: ConnectionOverview[];
  i18nTitle: string;
  i18nSubtitle: string;
  getConnectionHref(connection: ConnectionOverview): H.LocationDescriptor;
}

export class IntegrationEditorChooseConnection extends React.Component<
  IIntegrationEditorChooseConnection
> {
  public render() {
    return (
      <>
        <PageHeader>
          {this.props.breadcrumb}
          <h1>{this.props.i18nTitle}</h1>
          <p>{this.props.i18nSubtitle}</p>
        </PageHeader>
        <ConnectionsWithToolbar
          error={this.props.error}
          loading={this.props.loading}
          connections={this.props.connections}
          getConnectionHref={this.props.getConnectionHref}
        />
      </>
    );
  }
}
