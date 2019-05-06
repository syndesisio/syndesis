import { PageSection } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink } from '../Layout';
import {
  IListViewToolbarProps,
  ListViewToolbar,
  SimplePageHeader,
} from '../Shared';

export interface IConnectionsListViewProps extends IListViewToolbarProps {
  linkToConnectionCreate: H.LocationDescriptor;
  i18nTitle: string;
  i18nDescription: string;
  i18nLinkCreateConnection: string;
}

export class ConnectionsListView extends React.Component<
  IConnectionsListViewProps
> {
  public render() {
    return (
      <>
        <SimplePageHeader
          i18nTitle={this.props.i18nTitle}
          i18nDescription={this.props.i18nDescription}
        />
        <PageSection noPadding={true} variant={'light'}>
          <ListViewToolbar {...this.props}>
            <div className="form-group">
              <ButtonLink
                href={this.props.linkToConnectionCreate}
                as={'primary'}
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
