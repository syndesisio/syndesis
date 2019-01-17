import { WithConnections, WithIntegrationHelpers } from '@syndesis/api';
import { Connection, Integration } from '@syndesis/models';
import { Breadcrumb, ContentWithSidebarLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../../../../containers';
import {
  IntegrationEditorChooseConnection,
  IntegrationEditorSidebar,
} from '../../../../components';
import resolvers from '../../../../resolvers';
import {
  getConfigureConnectionHrefCallback,
  getConfigureStepHrefCallback,
  getCreateAddConnectionHref,
  getCreateAddStepHref,
  getCreateSelectActionHref,
} from '../../../resolversHelpers';

export interface ISelectConnectionRouteParams {
  position: string;
  connectionId: string;
}

export interface ISelectConnectionRouteState {
  connection: Connection;
  integration: Integration;
}

export class SelectConnectionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouteData<
          ISelectConnectionRouteParams,
          ISelectConnectionRouteState
        >>
          {({ position }, { connection, integration }) => (
            <ContentWithSidebarLayout
              sidebar={
                <WithIntegrationHelpers>
                  {({ getSteps }) => {
                    const positionAsNumber = parseInt(position, 10);
                    return (
                      <IntegrationEditorSidebar
                        steps={getSteps(integration, 0)}
                        addConnectionHref={getCreateAddConnectionHref.bind(
                          null,
                          integration
                        )}
                        configureConnectionHref={getConfigureConnectionHrefCallback(
                          integration
                        )}
                        configureStepHref={getConfigureStepHrefCallback(
                          integration
                        )}
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
                      connections={data.connectionsWithToAction}
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
