import { WithConnections } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import {
  ContentWithSidebarLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as H from 'history';
import * as React from 'react';
import { WithClosedNavigation } from '../../../containers';
import { IntegrationEditorChooseConnection } from '../components';
import { IntegrationCreatorBreadcrumbs } from '../components/IntegrationCreatorBreadcrumbs';
import resolvers from '../resolvers';

export function getFinishSelectActionHref(
  startConnection: ConnectionOverview,
  startAction: Action,
  integration: Integration,
  connection: ConnectionOverview
): H.LocationDescriptor {
  return resolvers.create.finish.selectAction({
    finishConnection: connection,
    integration,
    startAction,
    startConnection,
  });
}

export interface IntegrationCreatorFinishConnectionRouteState {
  startConnection: ConnectionOverview;
  startAction: Action;
  integration: Integration;
}

export class IntegrationCreatorFinishConnectionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouteData<null, IntegrationCreatorFinishConnectionRouteState>>
          {(_, { startConnection, startAction, integration }) => (
            <ContentWithSidebarLayout
              sidebar={
                <IntegrationVerticalFlow disabled={true}>
                  {({ expanded }) => (
                    <>
                      <IntegrationFlowStepWithOverview
                        icon={
                          <img
                            src={startConnection.icon}
                            width={24}
                            height={24}
                          />
                        }
                        i18nTitle={`1. ${startAction.name}`}
                        i18nTooltip={`1. ${startAction.name}`}
                        active={false}
                        showDetails={expanded}
                        name={startConnection.connector!.name}
                        action={startAction.name}
                        dataType={'TODO'}
                      />
                      <IntegrationFlowStepGeneric
                        icon={'+'}
                        i18nTitle={'2. Finish'}
                        i18nTooltip={'Finish'}
                        active={true}
                        showDetails={expanded}
                        description={'Choose a connection'}
                      />
                    </>
                  )}
                </IntegrationVerticalFlow>
              }
              content={
                <WithConnections>
                  {({ data, hasData, error }) => (
                    <IntegrationEditorChooseConnection
                      breadcrumb={
                        <IntegrationCreatorBreadcrumbs
                          step={4}
                          startConnection={startConnection}
                          startAction={startAction}
                          integration={integration}
                        />
                      }
                      i18nTitle={'Choose a Finish Connection'}
                      i18nSubtitle={
                        'Click the connection that completes the integration. If the connection you need is not available, click Create Connection.'
                      }
                      connections={data.connectionsWithToAction}
                      loading={!hasData}
                      error={error}
                      getConnectionHref={getFinishSelectActionHref.bind(
                        null,
                        startConnection,
                        startAction,
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
