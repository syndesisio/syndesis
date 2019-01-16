import { WithConnection, WithIntegrationHelpers } from '@syndesis/api';
import { ConnectionOverview, Integration } from '@syndesis/models';
import { Breadcrumb, ContentWithSidebarLayout, Loader } from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import {
  IntegrationEditorChooseAction,
  IntegrationEditorSidebar,
} from '../components';
import resolvers from '../resolvers';
import {
  getCreateAddConnectionHref,
  getCreateAddStepHref,
  getCreateConfigureActionHref,
  getCreateEditConnectionHref,
} from './resolversHelpers';

export interface IIntegrationCreatorSelectActionRouteParams {
  connectionId: string;
  position: string;
}

export interface IIntegrationCreatorSelectActionRouteState {
  connection: ConnectionOverview;
  integration: Integration;
}

export class IntegrationCreatorSelectActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouteData<
          IIntegrationCreatorSelectActionRouteParams,
          IIntegrationCreatorSelectActionRouteState
        >>
          {({ connectionId, position }, { connection, integration }) => (
            <WithConnection id={connectionId} initialValue={connection}>
              {({ data, hasData, error }) => (
                <WithLoader
                  error={error}
                  loading={!hasData}
                  loaderChildren={<Loader />}
                  errorChildren={<div>TODO</div>}
                >
                  {() => (
                    <ContentWithSidebarLayout
                      sidebar={
                        <WithIntegrationHelpers>
                          {({ getSteps }) => {
                            const positionAsNumber = parseInt(position, 10);
                            const configureConnectionHref = (idx: number) =>
                              getCreateEditConnectionHref(
                                `${idx}`,
                                integration
                              );
                            const configureStepHref = (idx: number) => 'TODO';
                            return (
                              <IntegrationEditorSidebar
                                steps={getSteps(integration, 0)}
                                addConnectionHref={getCreateAddConnectionHref.bind(
                                  null,
                                  integration
                                )}
                                configureConnectionHref={
                                  configureConnectionHref
                                }
                                configureStepHref={configureStepHref}
                                addStepHref={getCreateAddStepHref.bind(
                                  null,
                                  integration
                                )}
                                addAtIndex={positionAsNumber}
                                addIcon={
                                  hasData ? (
                                    <img
                                      src={data.icon}
                                      width={24}
                                      height={24}
                                    />
                                  ) : (
                                    <Loader />
                                  )
                                }
                                addI18nTitle={
                                  hasData
                                    ? `${positionAsNumber + 1}. ${
                                        data.connector!.name
                                      }`
                                    : `${positionAsNumber + 1}. Start`
                                }
                                addI18nTooltip={
                                  hasData
                                    ? `${positionAsNumber + 1}. ${data.name}`
                                    : 'Start'
                                }
                                addI18nDescription={'Choose an action'}
                              />
                            );
                          }}
                        </WithIntegrationHelpers>
                      }
                      content={
                        <IntegrationEditorChooseAction
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
                              <span>Choose action</span>
                            </Breadcrumb>
                          }
                          actions={data.actionsWithTo.sort((a, b) =>
                            a.name.localeCompare(b.name)
                          )}
                          getActionHref={getCreateConfigureActionHref.bind(
                            null,
                            position,
                            integration,
                            data
                          )}
                        />
                      }
                    />
                  )}
                </WithLoader>
              )}
            </WithConnection>
          )}
        </WithRouteData>
      </WithClosedNavigation>
    );
  }
}
