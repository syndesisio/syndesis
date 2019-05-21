import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Container } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';
import { toTestId } from '../../utils';

export interface IDvConnectionsListViewProps extends IListViewToolbarProps {
  linkToConnectionCreate: H.LocationDescriptor;
  i18nLinkCreateConnection: string;
}

export class DvConnectionsListView extends React.Component<
  IDvConnectionsListViewProps
> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <ButtonLink
              data-testid={`${toTestId(
                DvConnectionsListView.name,
                'create-connection-button'
              )}`}
              href={this.props.linkToConnectionCreate}
              as={'primary'}
            >
              {this.props.i18nLinkCreateConnection}
            </ButtonLink>
          </div>
        </ListViewToolbar>
        <Container>{this.props.children}</Container>
      </>
    );
  }
}
