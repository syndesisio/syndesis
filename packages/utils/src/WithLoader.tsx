import * as React from 'react';

export interface IWithLoaderProps {
  error: boolean;
  loading: boolean;
  minWait?: number;
  loaderChildren: JSX.Element;
  errorChildren: JSX.Element;
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

  protected waitTimeout?: number;

  constructor(props: IWithLoaderProps) {
    super(props);
    this.state = {
      loaded: !this.props.loading,
    };
  }

  public componentWillReceiveProps(nextProps: IWithLoaderProps) {
    this.setState({
      loaded: !nextProps.loading,
    });
  }

  public componentDidUpdate(prevProps: IWithLoaderProps) {
    if (!this.props.loading && !this.waitTimeout) {
      this.setTimeout();
    }
  }

  public render() {
    if (this.props.error) {
      return this.props.errorChildren;
    }
    if (!this.state.loaded) {
      return this.props.loaderChildren;
    }
    return this.props.children();
  }

  protected setTimeout() {
    this.clearTimeout();
    this.waitTimeout = window.setTimeout(() => {
      this.setState({
        loaded: true,
      });
    }, this.props.minWait!);
  }

  protected clearTimeout() {
    if (this.waitTimeout) {
      clearTimeout(this.waitTimeout);
      this.waitTimeout = undefined;
    }
  }
}
