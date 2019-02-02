import { WithConnection, WithIntegrationHelpers } from '@syndesis/api';
import { ConnectionOverview, Integration } from '@syndesis/models';
import { IntegrationEditorLayout, Loader } from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../../containers/PageTitle';
import {
  IntegrationEditorBreadcrumbs,
  IntegrationEditorChooseAction,
  IntegrationEditorSidebar,
} from '../../../../components';
import resolvers from '../../../../resolvers';
import { getEditConfigureActionHref } from '../../../resolversHelpers';

/**
 * @param connectionId - the ID of the connection selected in the previous step
 * @param position - the zero-based position for the new step in the integration
 * flow.
 */
export interface ISelectActionRouteParams {
  connectionId: string;
  position: string;
}

/**
 * @param integration - the integration object, used to render the IVP.
 * @param connection - the connection object selected in the previous step, used
 * to render the IVP.
 */
export interface ISelectActionRouteState {
  connection: ConnectionOverview;
  integration: Integration;
}

/**
 * This page shows the list of actions of a connection containing with a **to**
 * pattern.
 *
 * This component expects some [params]{@link ISelectActionRouteParams} and
 * [state]{@link ISelectActionRouteState} to be properly set in the route
 * object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 *
 * @todo DRY the connection icon code
 */
export class SelectActionPage extends React.Component {
  public render() {
    return (
      <WithRouteData<ISelectActionRouteParams, ISelectActionRouteState>>
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
                  <>
                    <PageTitle title={'Choose an action'} />
                    <IntegrationEditorLayout
                      header={<IntegrationEditorBreadcrumbs step={1} />}
                      sidebar={
                        <WithIntegrationHelpers>
                          {({ getSteps }) => {
                            const positionAsNumber = parseInt(position, 10);
                            return (
                              <IntegrationEditorSidebar
                                steps={getSteps(integration, 0)}
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
                          connectionName={connection.name}
                          actions={data.actionsWithTo.sort((a, b) =>
                            a.name.localeCompare(b.name)
                          )}
                          getActionHref={getEditConfigureActionHref.bind(
                            null,
                            position,
                            integration,
                            data
                          )}
                        />
                      }
                      cancelHref={resolvers.integration.edit.index({
                        integration,
                      })}
                      backHref={resolvers.integration.edit.addConnection.selectConnection(
                        { position, integration }
                      )}
                    />
                  </>
                )}
              </WithLoader>
            )}
          </WithConnection>
        )}
      </WithRouteData>
    );
  }
}
