import { DataList } from '@patternfly/react-core';
import * as React from 'react';

export interface ICiCdListProps {
  children: any;
}

export const CiCdList: React.FunctionComponent<ICiCdListProps> = props => {
  return <DataList aria-label={'cicd list'}>{props.children}</DataList>;
};
