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

  public render() {
    if (this.props.error) {
      return this.props.errorChildren;
    }
    if (!this.state.loaded) {
      return this.props.loading;
    }
    return this.props.children();
  }
}
