import * as H from 'history';
import * as React from 'react';
import { ButtonLink, Container } from '../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../Shared';

export interface IConnectionsListViewProps extends IListViewToolbarProps {
  linkToConnectionCreate: H.LocationDescriptor;
  i18nLinkCreateConnection: H.LocationDescriptor;
}

export class ConnectionsListView extends React.Component<
  IConnectionsListViewProps
> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <ButtonLink href={this.props.linkToConnectionCreate} as={'primary'}>
              {this.props.i18nLinkCreateConnection}
            </ButtonLink>
          </div>
        </ListViewToolbar>
        <Container>{this.props.children}</Container>
      </>
    );
  }
}
