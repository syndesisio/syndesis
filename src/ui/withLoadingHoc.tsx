import * as React from 'react';

interface IWithLoadingProps {
  loading: boolean;
}

export const withLoadingHoc = <P extends object>(Component: React.ComponentType<P>) =>
  class WithLoading extends React.Component<P & IWithLoadingProps> {
    public render() {
      const {loading, ...props} = this.props as IWithLoadingProps;
      return loading
        ? <div className="spinner"/>
        : <Component {...props} />;
    }
  };