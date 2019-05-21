import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export interface IWithUserHelpersChildrenProps {
  logout(): Promise<any>;
}
export interface IWithUserHelpersProps {
  children(props: IWithUserHelpersChildrenProps): any;
}

export class WithUserHelpersWrapped extends React.Component<
  IWithUserHelpersProps & IApiContext
> {
  constructor(props: IWithUserHelpersProps & IApiContext) {
    super(props);
    this.logout = this.logout.bind(this);
  }

  public async logout() {
    const response = await callFetch({
      headers: {
        ...this.props.headers,
        responseType: 'arraybuffer',
      },
      method: 'GET',
      url: `${this.props.apiUri}/logout`,
    });
    const html = String.fromCharCode.apply(null, new Uint8Array(response.body));
    window.history.pushState(null, null, '/logout');
    window.document.open();
    window.document.write(html);
    window.document.close();
  }

  public render() {
    return this.props.children({
      logout: this.logout,
    });
  }
}

export const WithUserHelpers: React.FunctionComponent<
  IWithUserHelpersProps
> = props => {
  return (
    <ApiContext.Consumer>
      {apiContext => <WithUserHelpersWrapped {...props} {...apiContext} />}
    </ApiContext.Consumer>
  );
};
