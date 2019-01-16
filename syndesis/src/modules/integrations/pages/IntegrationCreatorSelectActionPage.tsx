import { WithConnection, WithIntegrationHelpers } from '@syndesis/api';
import { ConnectionOverview, Integration, Step } from '@syndesis/models';
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
  constructor(props: any) {
    super(props);
    this.onAddStep = this.onAddStep.bind(this);
    this.onAddConnection = this.onAddConnection.bind(this);
    this.hideSidebarTooltips = this.hideSidebarTooltips.bind(this);
  }

  public onAddStep(e: React.MouseEvent<HTMLButtonElement>) {
    e.stopPropagation();
    this.setState({
      forceSidebarTooltips: true,
    });
  }

  public onAddConnection(e: React.MouseEvent<HTMLButtonElement>) {
    e.stopPropagation();
    this.setState({
      forceSidebarTooltips: true,
    });
  }

  public hideSidebarTooltips() {
    this.setState({
      forceSidebarTooltips: false,
    });
  }

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
                      onClick={this.hideSidebarTooltips}
                      sidebar={
                        <WithIntegrationHelpers>
                          {({ getSteps }) => {
                            const configureConnectionHref = (
                              idx: number,
                              step: Step
                            ) =>
                              getCreateConfigureActionHref(
                                `${idx}`,
                                integration,
                                step.connection!,
                                step.action!
                              );
                            const configureStepHref = (
                              idx: number,
                              step: Step
                            ) => 'TODO';
                            return (
                              <IntegrationEditorSidebar
                                disabled={true}
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
                                addAtIndex={parseInt(position, 10)}
                                addI18nTitle={
                                  hasData
                                    ? `1. ${data.connector!.name}`
                                    : '1. Start'
                                }
                                addI18nTooltip={
                                  hasData ? `1. ${data.name}` : 'Start'
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
                          actions={data.actionsWithFrom.sort((a, b) =>
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
