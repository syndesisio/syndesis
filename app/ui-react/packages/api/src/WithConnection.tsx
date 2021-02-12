import { Action, ConnectionOverview } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { getActionsWithPattern } from './helpers';
import { SyndesisFetch } from './SyndesisFetch';

export interface IConnectionOverviewExtended extends ConnectionOverview {
  readonly actionsWithFrom: Action[];
  readonly actionsWithPipe: Action[];
  readonly actionsWithTo: Action[];
  readonly actionsWithPollEnrich: Action[];
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
              actionsWithFrom: getActionsWithPattern(
                'From',
                response.data.connector ? response.data.connector.actions : []
              ),
              actionsWithPipe: getActionsWithPattern(
                'Pipe',
                response.data.connector ? response.data.connector.actions : []
              ),
              actionsWithPollEnrich: getActionsWithPattern(
                'PollEnrich',
                response.data.connector ? response.data.connector.actions : []
              ),
              actionsWithTo: getActionsWithPattern(
                'To',
                response.data.connector ? response.data.connector.actions : []
              ),
            },
          })
        }
      </SyndesisFetch>
    );
  }
}
