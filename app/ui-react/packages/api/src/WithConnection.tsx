import { Action, ConnectionOverview } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { getActionsWithFrom, getActionsWithTo } from './helpers';
import { SyndesisFetch } from './SyndesisFetch';

export interface IConnectionOverviewExtended extends ConnectionOverview {
  readonly actionsWithFrom: Action[];
  readonly actionsWithTo: Action[];
}

export interface IWithConnectionProps {
  id: string;
  initialValue?: ConnectionOverview;
  children(props: IFetchState<IConnectionOverviewExtended>): any;
}

export class WithConnection extends React.Component<IWithConnectionProps> {
  public render() {
    return (
      <SyndesisFetch<ConnectionOverview>
        url={`/connections/${this.props.id}`}
        defaultValue={{
          name: '',
        }}
        initialValue={this.props.initialValue}
      >
        {({ response }) =>
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
      </SyndesisFetch>
    );
  }
}
