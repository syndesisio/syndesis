import { getSteps, WithConnection } from '@syndesis/api';
import {
  ButtonLink,
  IntegrationEditorActionsListItem,
  IntegrationEditorChooseAction,
  IntegrationEditorLayout,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { ApiError, PageTitle } from '../../../../../../shared';
import {
  IntegrationEditorBreadcrumbs,
  IntegrationEditorSidebar,
} from '../../../../components';
import resolvers from '../../../../resolvers';
import {
  ISelectActionRouteParams,
  ISelectActionRouteState,
} from '../../../editorInterfaces';

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
        {({ connectionId, flow, position }, { connection, integration }) => {
          const positionAsNumber = parseInt(position, 10);
          return (
            <WithConnection id={connectionId} initialValue={connection}>
              {({ data, hasData, error }) => (
                <WithLoader
                  error={error}
                  loading={!hasData}
                  loaderChildren={<Loader />}
                  errorChildren={<ApiError />}
                >
                  {() => (
                    <>
                      <PageTitle title={'Choose an action'} />
                      <IntegrationEditorLayout
                        header={<IntegrationEditorBreadcrumbs step={1} />}
                        sidebar={
                          <IntegrationEditorSidebar
                            steps={getSteps(integration, 0)}
                            addAtIndex={positionAsNumber}
                            addIcon={
                              hasData ? (
                                <img src={data.icon} width={24} height={24} />
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
                        }
                        content={
                          <IntegrationEditorChooseAction
                            i18nTitle={`${connection.name} - Choose Action`}
                            i18nSubtitle={
                              'Choose an action for the selected connectionName.'
                            }
                          >
                            {data.actionsWithTo
                              .sort((a, b) => a.name.localeCompare(b.name))
                              .map((a, idx) => (
                                <IntegrationEditorActionsListItem
                                  key={idx}
                                  integrationName={a.name}
                                  integrationDescription={
                                    a.description || 'No description available.'
                                  }
                                  actions={
                                    <ButtonLink
                                      href={resolvers.integration.edit.addStep.configureAction(
                                        {
                                          actionId: a.id!,
                                          connection,
                                          flow,
                                          integration,
                                          position,
                                        }
                                      )}
                                    >
                                      Select
                                    </ButtonLink>
                                  }
                                />
                              ))}
                          </IntegrationEditorChooseAction>
                        }
                        cancelHref={resolvers.integration.edit.index({
                          flow,
                          integration,
                        })}
                        backHref={resolvers.integration.edit.addStep.selectConnection(
                          { flow, position, integration }
                        )}
                      />
                    </>
                  )}
                </WithLoader>
              )}
            </WithConnection>
          );
        }}
      </WithRouteData>
    );
  }
}
