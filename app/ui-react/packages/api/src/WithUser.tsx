import { User } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithUserProps {
  children(props: IFetchState<User>): any;
}

export class WithUser extends React.Component<IWithUserProps> {
  public render() {
    return (
      <SyndesisFetch<User>
        url={'/users/~'}
        defaultValue={{ username: 'Unknown user' }}
      >
        {({ response }) => this.props.children({ ...response })}
      </SyndesisFetch>
    );
  }
}
