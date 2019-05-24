import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface IOAuthAppListProps {
  children: any;
}

export const OAuthAppList: React.FunctionComponent<IOAuthAppListProps> = ({
  children,
}) => <ListView>{children}</ListView>;
