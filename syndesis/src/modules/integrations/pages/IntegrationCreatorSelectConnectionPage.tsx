import { WithConnections, WithIntegrationHelpers } from '@syndesis/api';
import { Connection, Integration } from '@syndesis/models';
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
  getCreateEditConnectionHref,
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
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouteData<
          IIntegrationCreatorSelectConnectionRouteParams,
          IIntegrationCreatorSelectConnectionRouteState
        >>
          {({ position }, { connection, integration }) => (
            <ContentWithSidebarLayout
              sidebar={
                <WithIntegrationHelpers>
                  {({ getSteps }) => {
                    const positionAsNumber = parseInt(position, 10);
                    const configureConnectionHref = (idx: number) =>
                      getCreateEditConnectionHref(`${idx}`, integration);
                    const configureStepHref = (idx: number) => 'TODO';
                    return (
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
                        addI18nTitle={`${positionAsNumber + 1}. Start`}
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
