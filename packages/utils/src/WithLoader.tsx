import * as React from 'react';

export interface IWithLoaderProps {
  loading: boolean;
  loader(loading: boolean): JSX.Element;
  minWait?: number;
  children(): any;
}

export interface IWithLoaderState {
  loaded: boolean;
}

export class WithLoader extends React.Component<
  IWithLoaderProps,
  IWithLoaderState
> {
  public static defaultProps = {
    minWait: 500,
  };

  protected waitInterval?: number;

  constructor(props: IWithLoaderProps) {
    super(props);
    this.state = {
      loaded: !this.props.loading,
    };
  }

  public componentDidUpdate(prevProps: IWithLoaderProps) {
    if (prevProps.loading && !this.props.loading) {
      this.waitInterval = window.setTimeout(() => {
        this.setState({
          loaded: true,
        });
      }, this.props.minWait!);
    }
  }

  render() {
    return this.state.loaded
      ? this.props.children()
      : this.props.loader(this.props.loading);
  }
}
