import {
  PageSection,
  TextContent,
  Title,
  TitleLevel,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink } from '../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../Shared';

export interface IConnectionsListViewProps extends IListViewToolbarProps {
  linkToConnectionCreate: H.LocationDescriptor;
  i18nTitle: string;
  i18nLinkCreateConnection: string;
}

export class ConnectionsListView extends React.Component<
  IConnectionsListViewProps
> {
  public render() {
    return (
      <>
        <PageSection variant={'light'}>
          <TextContent>
            <Title size={'2xl'} headingLevel={TitleLevel.h1}>
              {this.props.i18nTitle}
            </Title>
          </TextContent>
        </PageSection>
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
