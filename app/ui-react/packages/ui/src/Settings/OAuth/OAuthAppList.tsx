import { DataList } from '@patternfly/react-core';
import * as React from 'react';

export interface IOAuthAppListProps {
  children: any;
}

export const OAuthAppList: React.FunctionComponent<IOAuthAppListProps> = ({
  children,
}) => <DataList aria-label={'oauth app list'}>{children}</DataList>;
