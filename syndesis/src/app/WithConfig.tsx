import * as React from 'react';
import defaultConfig, { IConfigFile } from './config';

export interface IWithConfigProps {
  children(props: IConfigFile): any;
}

export class WithConfig extends React.Component<IWithConfigProps, IConfigFile> {
  public state = defaultConfig;

  public render() {
    return this.props.children(this.state);
  }
}
