import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface ICiCdListProps {
  children: any;
}

export class CiCdList extends React.Component<ICiCdListProps> {
  public render() {
    return <ListView>{this.props.children}</ListView>;
  }
}
