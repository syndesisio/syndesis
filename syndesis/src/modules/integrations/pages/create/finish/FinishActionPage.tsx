import { WithConnection } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import {
  IntegrationEditorLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../containers/PageTitle';
import {
  IntegrationCreatorBreadcrumbs,
  IntegrationEditorChooseAction,
} from '../../../components';
import resolvers from '../../../resolvers';
import { getFinishConfigureActionHref } from '../../resolversHelpers';

/**
 * @param connectionId - the ID of the connection selected in step 2.1, whose
 * actions should be shown.
 */
export interface IFinishActionRouteParams {
  connectionId: string;
}

/**
 * @param startConnection - the connection object selected in step 1.1. Needed
 * to render the IVP.
 * @param startAction - the action object selected in step 1.2. Needed to
 * render the IVP.
 * @oaram integration - the integration object created in step 1.3.
 * @param finishConnection - the connection object selected in step 2.1. Needed
 * to render the IVP.
 */
export interface IFinishActionRouteState {
  startConnection: ConnectionOverview;
  startAction: Action;
  integration: Integration;
  finishConnection: ConnectionOverview;
}

/**
 * This page shows the list of actions with a **to** pattern of a connection.
 * It's supposed to be used for step 2.2 of the creation wizard.
 *
 * This component expects some [url params]{@link IFinishActionRouteParams}
 * and [state]{@link IFinishActionRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 *
 * @todo DRY the connection icon code
 */
export class FinishActionPage extends React.Component {
  public render() {
    return (
      <WithRouteData<IFinishActionRouteParams, IFinishActionRouteState>>
        {(
          { connectionId },
          { startConnection, startAction, integration, finishConnection }
        ) => (
          <WithConnection id={connectionId} initialValue={finishConnection}>
            {({ data, hasData, error }) => (
              <>
                <PageTitle title={'Choose an action'} />
                <IntegrationEditorLayout
                  header={
                    <IntegrationCreatorBreadcrumbs step={2} subStep={1} />
                  }
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
                            icon={
                              hasData ? (
                                <img src={data.icon} width={24} height={24} />
                              ) : (
                                <Loader />
                              )
                            }
                            i18nTitle={
                              hasData
                                ? `2. ${data.connector!.name}`
                                : '2. Finish'
                            }
                            i18nTooltip={hasData ? `2. ${data.name}` : 'Finish'}
                            active={true}
                            showDetails={expanded}
                            description={'Choose an action'}
                          />
                        </>
                      )}
                    </IntegrationVerticalFlow>
                  }
                  content={
                    <WithLoader
                      error={error}
                      loading={!hasData}
                      loaderChildren={<Loader />}
                      errorChildren={<div>TODO</div>}
                    >
                      {() => (
                        <IntegrationEditorChooseAction
                          connectionName={data.name}
                          actions={data.actionsWithTo.sort((a, b) =>
                            a.name.localeCompare(b.name)
                          )}
                          getActionHref={getFinishConfigureActionHref.bind(
                            null,
                            startConnection,
                            startAction,
                            finishConnection,
                            integration
                          )}
                        />
                      )}
                    </WithLoader>
                  }
                  backHref={resolvers.create.finish.selectConnection({
                    integration,
                    startConnection,
                    startAction,
                  })}
                  cancelHref={resolvers.list()}
                />
              </>
            )}
          </WithConnection>
        )}
      </WithRouteData>
    );
  }
}
