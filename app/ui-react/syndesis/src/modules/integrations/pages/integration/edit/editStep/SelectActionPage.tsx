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
 * This page shows the list of actions of a connection containing either a
 * **to** or **from pattern, depending on the specified [position]{@link ISelectActionRouteParams#position}.
 *
 * This component expects some [params]{@link ISelectActionRouteParams} and
 * [state]{@link ISelectActionRouteState} to be properly set in the route
 * object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
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
                            activeIndex={positionAsNumber}
                          />
                        }
                        content={
                          <IntegrationEditorChooseAction
                            i18nTitle={`${connection.name} - Choose Action`}
                            i18nSubtitle={
                              'Choose an action for the selected connectionName.'
                            }
                          >
                            {(positionAsNumber > 0
                              ? data.actionsWithTo
                              : data.actionsWithFrom
                            )
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
                                      href={resolvers.integration.edit.editStep.configureAction(
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
