import * as React from 'react';
import { IConfigFile } from './config';

export interface IWithConfigProps {
  children(props: IWithConfigState): any;
}

export interface IWithConfigState {
  loading: boolean;
  config?: IConfigFile;
}

export class WithConfig extends React.Component<
  IWithConfigProps,
  IWithConfigState
> {
  public state = {
    loading: true,
  };

  public async componentDidMount() {
    try {
      const configResponse = await fetch('/config.json');
      const config = await configResponse.json();
      this.setState({
        config,
        loading: false,
      });
    } catch (e) {
      const { default: config } = await import('./config');
      this.setState({
        config,
        loading: false,
      });
    }
  }

  public render() {
    return this.props.children(this.state);
  }
}
