import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';
import resolvers from '../resolvers';

export interface IIntegrationCreatorBreadcrumbsProps {
  step: number;
  finishConnection?: ConnectionOverview;
  integration?: Integration;
  startAction?: Action;
  startConnection?: ConnectionOverview;
}

export const IntegrationCreatorBreadcrumbs: React.FunctionComponent<
  IIntegrationCreatorBreadcrumbsProps
> = ({ step, finishConnection, integration, startAction, startConnection }) => {
  const links = [
    (lastItem: boolean, label: string = 'Integrations') =>
      lastItem ? (
        label
      ) : (
        <Link to={resolvers.list()} key={1}>
          {label}
        </Link>
      ),
    (lastItem: boolean, label: string = 'New integration') =>
      lastItem ? (
        label
      ) : (
        <Link to={resolvers.create.start.selectConnection()} key={2}>
          {label}
        </Link>
      ),
    (lastItem: boolean, label: string = 'Start connection') =>
      lastItem ? (
        label
      ) : (
        <Link
          to={resolvers.create.start.selectAction({
            connection: startConnection!,
          })}
          key={3}
        >
          {label}
        </Link>
      ),
    (lastItem: boolean, label: string = 'Configure action') =>
      lastItem ? (
        label
      ) : (
        <Link
          to={resolvers.create.start.configureAction({
            actionId: startAction!.id!,
            connection: startConnection!,
          })}
          key={4}
        >
          {label}
        </Link>
      ),
    (lastItem: boolean, label: string = 'Finish Connection') =>
      lastItem ? (
        label
      ) : (
        <Link
          to={resolvers.create.finish.selectConnection({
            integration: integration!,
            startAction: startAction!,
            startConnection: startConnection!,
          })}
          key={5}
        >
          {label}
        </Link>
      ),
    (lastItem: boolean, label: string = 'Choose Action') =>
      lastItem ? (
        label
      ) : (
        <Link
          to={resolvers.create.finish.selectAction({
            finishConnection: finishConnection!,
            integration: integration!,
            startAction: startAction!,
            startConnection: startConnection!,
          })}
          key={6}
        >
          {label}
        </Link>
      ),
    (lastItem: boolean, label: string = '') => (
      <span key={7}>Configure action</span>
    ),
  ];

  return (
    <Breadcrumb>
      {links.slice(0, step + 1).map((l, index) => l(index === step))}
    </Breadcrumb>
  );
};
