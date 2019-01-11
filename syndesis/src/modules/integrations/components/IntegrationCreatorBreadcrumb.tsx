import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';
import resolvers from '../resolvers';

export interface IIntegrationCreatorBreadcrumbProps {
  startConnection: ConnectionOverview;
  startAction: Action;
  integration: Integration;
  finishConnection: ConnectionOverview;
}

export const IntegrationCreatorBreadcrumb: React.FunctionComponent<
  IIntegrationCreatorBreadcrumbProps
> = ({ startConnection, startAction, integration, finishConnection }) => (
  <Breadcrumb>
    <Link to={resolvers.list()}>Integrations</Link>
    <Link to={resolvers.create.start.selectConnection()}>New integration</Link>
    <Link
      to={resolvers.create.start.selectAction({
        connection: startConnection,
      })}
    >
      Start connection
    </Link>
    <Link
      to={resolvers.create.start.configureAction({
        actionId: startAction.id!,
        connection: startConnection,
      })}
    >
      Configure action
    </Link>
    <Link
      to={resolvers.create.finish.selectConnection({
        integration,
        startAction,
        startConnection,
      })}
    >
      Finish Connection
    </Link>
    <Link
      to={resolvers.create.finish.selectAction({
        finishConnection,
        integration,
        startAction,
        startConnection,
      })}
    >
      Choose Action
    </Link>
    <span>Configure action</span>
  </Breadcrumb>
);
