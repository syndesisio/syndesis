import { WithConnections } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import {
  IntegrationEditorLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../containers/PageTitle';
import {
  IntegrationCreatorBreadcrumbs,
  IntegrationEditorChooseConnection,
} from '../../../components';
import resolvers from '../../../resolvers';
import { getFinishSelectActionHref } from '../../resolversHelpers';

/**
 * @param startConnection - the connection object selected in step 1.1. Needed
 * to render the IVP.
 * @param startAction - the action object selected in step 1.2. Needed to
 * render the IVP.
 * @oaram integration - the integration object created in step 1.3.
 */
export interface IFinishConnectionRouteState {
  startConnection: ConnectionOverview;
  startAction: Action;
  integration: Integration;
}

/**
 * This page shows the list of connections containing actions with a **to**
 * pattern.
 * It's supposed to be used for step 2.1 of the creation wizard.
 *
 * This component expects some [state]{@link IFinishConnectionRouteState} to be
 * properly set in the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 *
 * @todo DRY the connection icon code
 */
export class FinishConnectionPage extends React.Component {
  public render() {
    return (
      <WithRouteData<null, IFinishConnectionRouteState>>
        {(_, { startConnection, startAction, integration }) => (
          <>
            <PageTitle title={'Choose a Finish Connection'} />
            <IntegrationEditorLayout
              header={<IntegrationCreatorBreadcrumbs step={2} />}
              sidebar={
                <IntegrationVerticalFlow>
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
                        icon={<i className={'fa fa-plus'} />}
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
              backHref={resolvers.create.start.configureAction({
                actionId: startAction.id!,
                connection: startConnection,
                integration,
              })}
              cancelHref={resolvers.list()}
            />
          </>
        )}
      </WithRouteData>
    );
  }
}
