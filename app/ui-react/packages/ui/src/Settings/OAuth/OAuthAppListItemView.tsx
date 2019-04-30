import * as React from 'react';
import { Container } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

export interface IOAuthAppListItemViewProps extends IListViewToolbarProps {
  children: React.ReactNode;
}

export const OAuthAppListItemView: React.FunctionComponent<
  IOAuthAppListItemViewProps
> = ({ children, ...rest }) => (
  <>
    <ListViewToolbar {...rest} />
    <Container>{children}</Container>
  </>
);
