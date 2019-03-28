import * as React from 'react';
import { ButtonLink, Container } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

export interface IDvConnectionsListViewProps extends IListViewToolbarProps {
  linkToConnectionCreate: string;
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
