import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import {
  ContentWithSidebarLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import * as React from 'react';
import { WithClosedNavigation } from '../../../containers';
import { IntegrationEditorChooseConnection } from '../components';
import { IntegrationCreatorBreadcrumbs } from '../components/IntegrationCreatorBreadcrumbs';
import resolvers from '../resolvers';

export function getStartSelectActionHref(connection: Connection) {
  return resolvers.create.start.selectAction({ connection });
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
                  <IntegrationFlowStepGeneric
                    icon={'+'}
                    i18nTitle={'1. Start'}
                    i18nTooltip={'Start'}
                    active={true}
                    showDetails={expanded}
                    description={'Choose a connection'}
                  />
                  <IntegrationFlowStepWithOverview
                    icon={'+'}
                    i18nTitle={'2. Finish'}
                    i18nTooltip={'Finish'}
                    active={false}
                    showDetails={expanded}
                    name={'n/a'}
                    action={'n/a'}
                    dataType={'n/a'}
                  />
                </>
              )}
            </IntegrationVerticalFlow>
          }
          content={
            <WithConnections>
              {({ data, hasData, error }) => (
                <IntegrationEditorChooseConnection
                  breadcrumb={<IntegrationCreatorBreadcrumbs step={1} />}
                  connections={data.connectionsWithFromAction}
                  loading={!hasData}
                  error={error}
                  i18nTitle={'Choose a Start Connection'}
                  i18nSubtitle={
                    'Click the connection that starts the integration. If the connection you need is not available, click Create Connection.'
                  }
                  getConnectionHref={getStartSelectActionHref}
                />
              )}
            </WithConnections>
          }
        />
      </WithClosedNavigation>
    );
  }
}
