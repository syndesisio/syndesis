import { WithIntegrationHelpers } from '@syndesis/api';
import { ConnectionOverview, Integration } from '@syndesis/models';
import { Breadcrumb, ContentWithSidebarLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import {
  IntegrationEditorConfigureConnection,
  IntegrationEditorSidebar,
} from '../components';
import { IWithAutoFormHelperOnUpdatedIntegrationProps } from '../containers';
import resolvers from '../resolvers';
import {
  getCreateAddConnectionHref,
  getCreateAddStepHref,
  getCreateEditConnectionHref,
} from './resolversHelpers';

export interface IIntegrationCreatorConfigureActionRouteParams {
  position: string;
  actionId: string;
  connectionId: string;
  step?: string;
}

export interface IIntegrationCreatorConfigureActionRouteState {
  connection: ConnectionOverview;
  integration: Integration;
}

export class IntegrationCreatorConfigureActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithIntegrationHelpers>
          {({ getSteps }) => (
            <WithRouteData<
              IIntegrationCreatorConfigureActionRouteParams,
              IIntegrationCreatorConfigureActionRouteState
            >>
              {(
                { actionId, connectionId, step = '0', position },
                { connection, integration },
                { history }
              ) => {
                const stepAsNumber = parseInt(step, 10);
                const onUpdatedIntegration = ({
                  updatedIntegration,
                  moreConfigurationSteps,
                }: IWithAutoFormHelperOnUpdatedIntegrationProps) => {
                  if (moreConfigurationSteps) {
                    history.push(
                      resolvers.create.configure.addConnection.configureAction({
                        actionId,
                        connection,
                        integration: updatedIntegration,
                        position,
                        step: stepAsNumber + 1,
                      })
                    );
                  } else {
                    history.push(
                      resolvers.create.configure.index({
                        integration: updatedIntegration,
                      })
                    );
                  }
                };
                const positionAsNumber = parseInt(position, 10);
                const configureConnectionHref = (idx: number) =>
                  getCreateEditConnectionHref(`${idx}`, integration);
                const configureStepHref = (idx: number) => 'TODO';
                return (
                  <ContentWithSidebarLayout
                    sidebar={
                      <IntegrationEditorSidebar
                        steps={getSteps(integration, 0)}
                        addConnectionHref={getCreateAddConnectionHref.bind(
                          null,
                          integration
                        )}
                        configureConnectionHref={configureConnectionHref}
                        configureStepHref={configureStepHref}
                        addStepHref={getCreateAddStepHref.bind(
                          null,
                          integration
                        )}
                        addAtIndex={positionAsNumber}
                        addIcon={
                          <img src={connection.icon} width={24} height={24} />
                        }
                        addI18nTitle={`${positionAsNumber + 1}. ${
                          connection.connector!.name
                        }`}
                        addI18nTooltip={`${positionAsNumber + 1}. ${
                          connection.name
                        }`}
                        addI18nDescription={'Configure the action'}
                      />
                    }
                    content={
                      <IntegrationEditorConfigureConnection
                        breadcrumb={
                          <Breadcrumb>
                            <Link to={resolvers.list()}>Integrations</Link>
                            <Link
                              to={resolvers.create.start.selectConnection()}
                            >
                              New integration
                            </Link>
                            <Link
                              to={resolvers.create.configure.index({
                                integration,
                              })}
                            >
                              Save or add step
                            </Link>
                            <Link
                              to={resolvers.create.configure.addConnection.selectConnection(
                                { position, integration }
                              )}
                            >
                              Choose a connection
                            </Link>
                            <Link
                              to={resolvers.create.configure.addConnection.selectAction(
                                { position, integration, connection }
                              )}
                            >
                              Choose action
                            </Link>
                            <span>Configure the action</span>
                          </Breadcrumb>
                        }
                        integration={integration}
                        connection={connection}
                        actionId={actionId}
                        configurationStep={stepAsNumber}
                        flow={0}
                        flowStep={1}
                        backLink={resolvers.create.configure.addConnection.selectAction(
                          { position, integration, connection }
                        )}
                        onUpdatedIntegration={onUpdatedIntegration}
                      />
                    }
                  />
                );
              }}
            </WithRouteData>
          )}
        </WithIntegrationHelpers>
      </WithClosedNavigation>
    );
  }
}
