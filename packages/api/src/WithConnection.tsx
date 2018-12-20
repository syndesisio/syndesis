import { Action, ConnectionOverview } from '@syndesis/models';
import * as React from 'react';
import { IRestState } from './Rest';
import { SyndesisRest } from './SyndesisRest';

export function getActionsWithFrom(actions: Action[] = []) {
  return actions.filter(a => a.pattern === 'From');
}

export function getActionsWithTo(actions: Action[] = []) {
  return actions.filter(a => a.pattern === 'To');
}

export interface IConnectionResponse extends ConnectionOverview {
  readonly actionsWithFrom: Action[];
  readonly actionsWithTo: Action[];
}

export interface IWithConnectionProps {
  id: string;
  children(props: IRestState<IConnectionResponse>): any;
}

export class WithConnection extends React.Component<IWithConnectionProps> {
  public render() {
    return (
      <SyndesisRest<ConnectionOverview>
        url={`/connections/${this.props.id}`}
        defaultValue={{
          name: '',
        }}
      >
        {({ read, response }) =>
          this.props.children({
            ...response,
            data: {
              ...response.data,
              actionsWithFrom: getActionsWithFrom(
                response.data.connector ? response.data.connector.actions : []
              ),
              actionsWithTo: getActionsWithTo(
                response.data.connector ? response.data.connector.actions : []
              ),
            },
          })
        }
      </SyndesisRest>
    );
  }
}
