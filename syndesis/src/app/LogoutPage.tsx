import * as React from 'react';

interface ILogoutProps {
  logout(): void;
}

export class Logout extends React.Component<ILogoutProps> {
  public componentWillMount() {
    this.props.logout();
  }

  public render() {
    return <p>Logging out...</p>;
  }
}
