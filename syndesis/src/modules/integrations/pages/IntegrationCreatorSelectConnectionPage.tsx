import { WithConnections, WithIntegrationHelpers } from '@syndesis/api';
import { Connection, Integration, Step } from '@syndesis/models';
import { Breadcrumb, ContentWithSidebarLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import {
  IntegrationEditorChooseConnection,
  IntegrationEditorSidebar,
} from '../components';
import resolvers from '../resolvers';
import {
  getCreateAddConnectionHref,
  getCreateAddStepHref,
  getCreateConfigureActionHref,
  getCreateSelectActionHref,
} from './resolversHelpers';

export interface IIntegrationCreatorSelectConnectionRouteParams {
  position: string;
  connectionId: string;
}

export interface IIntegrationCreatorSelectConnectionRouteState {
  connection: Connection;
  integration: Integration;
}

export class IntegrationCreatorSelectConnectionPage extends React.Component {
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
          IIntegrationCreatorSelectConnectionRouteParams,
          IIntegrationCreatorSelectConnectionRouteState
        >>
          {({ position }, { connection, integration }) => (
            <ContentWithSidebarLayout
              onClick={this.hideSidebarTooltips}
              sidebar={
                <WithIntegrationHelpers>
                  {({ getSteps }) => {
                    const configureConnectionHref = (idx: number, step: Step) =>
                      getCreateConfigureActionHref(
                        `${idx}`,
                        integration,
                        step.connection!,
                        step.action!
                      );
                    const configureStepHref = (idx: number, step: Step) =>
                      'TODO';
                    return (
                      <IntegrationEditorSidebar
                        disabled={true}
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
                        addAtIndex={parseInt(position, 10)}
                        addI18nTitle={'1. Start'}
                        addI18nTooltip={'Start'}
                        addI18nDescription={'Choose a connection'}
                      />
                    );
                  }}
                </WithIntegrationHelpers>
              }
              content={
                <WithConnections>
                  {({ data, hasData, error }) => (
                    <IntegrationEditorChooseConnection
                      breadcrumb={
                        <Breadcrumb>
                          <Link to={resolvers.list()}>Integrations</Link>
                          <Link to={resolvers.create.start.selectConnection()}>
                            New integration
                          </Link>
                          <Link
                            to={resolvers.create.configure.index({
                              integration,
                            })}
                          >
                            Save or add step
                          </Link>
                          <span>Choose a connection</span>
                        </Breadcrumb>
                      }
                      connections={data.connectionsWithFromAction}
                      loading={!hasData}
                      error={error}
                      i18nTitle={'Choose a connection'}
                      i18nSubtitle={
                        'Click the connection that completes the integration. If the connection you need is not available, click Create Connection.'
                      }
                      getConnectionHref={getCreateSelectActionHref.bind(
                        null,
                        position,
                        integration
                      )}
                    />
                  )}
                </WithConnections>
              }
            />
          )}
        </WithRouteData>
      </WithClosedNavigation>
    );
  }
}
