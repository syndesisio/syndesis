import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, PageSection } from '../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../Shared';

export interface IConnectionsListViewProps extends IListViewToolbarProps {
  createConnectionButtonStyle?: 'primary' | 'default';
  linkToConnectionCreate: H.LocationDescriptor;
  i18nLinkCreateConnection: string;
}

export class ConnectionsListView extends React.Component<
  IConnectionsListViewProps
> {
  public render() {
    return (
      <>
        <PageSection noPadding={true} variant={'light'}>
          <ListViewToolbar {...this.props}>
            <div className="form-group">
              <ButtonLink
                data-testid={'connections-list-view-create-connection-button'}
                href={this.props.linkToConnectionCreate}
                as={this.props.createConnectionButtonStyle || 'primary'}
              >
                {this.props.i18nLinkCreateConnection}
              </ButtonLink>
            </div>
          </ListViewToolbar>
        </PageSection>
        <PageSection>{this.props.children}</PageSection>
      </>
    );
  }
}
