import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import {
  Breadcrumb,
  ContentWithSidebarLayout,
  IntegrationFlowStep,
  IntegrationFlowStepDetails,
  IntegrationVerticalFlow,
  PageHeader,
} from '@syndesis/ui';
import { reverse } from 'named-urls';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import { ConnectionsWithToolbar } from '../../connections/containers/ConnectionsWithToolbar';
import routes from '../routes';

export function getStartSelectActionHref(connection: Connection): string {
  return reverse(routes.integrations.create.start.selectAction, {
    connectionId: connection.id,
  });
}

export class IntegrationCreatorStartConnectionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <ContentWithSidebarLayout
          sidebar={
            <IntegrationVerticalFlow disabled={true}>
              {({ expanded }) => (
                <>
                  <IntegrationFlowStep
                    icon={'+'}
                    active={true}
                    i18nTooltip={'Start'}
                    showDetails={expanded}
                  >
                    <IntegrationFlowStepDetails active={true}>
                      {({ Title, GenericDescription }) => (
                        <>
                          <Title>Start</Title>
                          <GenericDescription>
                            Choose a connection
                          </GenericDescription>
                        </>
                      )}
                    </IntegrationFlowStepDetails>
                  </IntegrationFlowStep>
                  <IntegrationFlowStep
                    icon={'+'}
                    i18nTooltip={'Finish'}
                    showDetails={expanded}
                  >
                    <IntegrationFlowStepDetails>
                      {({ Title, StepOverview }) => (
                        <>
                          <Title>Start</Title>
                          <StepOverview
                            nameI18nLabel={'Name:'}
                            name={'n/a'}
                            actionI18nLabel={'Action:'}
                            action={'n/a'}
                            dataTypeI18nLabel={'Data Type:'}
                            dataType={'n/a'}
                          />
                        </>
                      )}
                    </IntegrationFlowStepDetails>
                  </IntegrationFlowStep>
                </>
              )}
            </IntegrationVerticalFlow>
          }
          content={
            <>
              <PageHeader>
                <Breadcrumb>
                  <Link to={routes.integrations.list}>Integrations</Link>
                  <span>New integration</span>
                </Breadcrumb>
                <h1>Choose a Start Connection</h1>
                <p>
                  Click the connection that starts the integration. If the
                  connection you need is not available, click Create Connection.
                </p>
              </PageHeader>
              <WithConnections>
                {({ data, hasData, error }) => (
                  <ConnectionsWithToolbar
                    error={error}
                    loading={!hasData}
                    connections={data.connectionsWithFromAction}
                    getConnectionHref={getStartSelectActionHref}
                  />
                )}
              </WithConnections>
            </>
          }
        />
      </WithClosedNavigation>
    );
  }
}
