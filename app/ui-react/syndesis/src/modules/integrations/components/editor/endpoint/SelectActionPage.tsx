import { getConnectionIcon, getSteps, WithConnection } from '@syndesis/api';
import * as H from '@syndesis/history';
import {
  ButtonLink,
  IntegrationEditorActionsListItem,
  IntegrationEditorChooseAction,
  IntegrationEditorLayout,
  PageLoader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { ApiError, PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  ISelectActionRouteParams,
  ISelectActionRouteState,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';

export interface ISelectActionPageProps {
  cancelHref: (
    p: ISelectActionRouteParams,
    s: ISelectActionRouteState
  ) => H.LocationDescriptor;
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  selectHref: (
    actionId: string,
    p: ISelectActionRouteParams,
    s: ISelectActionRouteState
  ) => H.LocationDescriptor;
}

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
export class SelectActionPage extends React.Component<ISelectActionPageProps> {
  public render() {
    return (
      <WithRouteData<ISelectActionRouteParams, ISelectActionRouteState>>
        {({ connectionId, flowId, position }, { connection, integration }) => {
          const positionAsNumber = parseInt(position, 10);
          return (
            <WithConnection id={connectionId} initialValue={connection}>
              {({ data, hasData, error }) => (
                <WithLoader
                  error={error}
                  loading={!hasData}
                  loaderChildren={<PageLoader />}
                  errorChildren={<ApiError />}
                >
                  {() => (
                    <>
                      <PageTitle title={'Choose an action'} />
                      <IntegrationEditorLayout
                        title={'Choose an action'}
                        description={
                          'Choose an action for the selected connection.'
                        }
                        sidebar={this.props.sidebar({
                          activeIndex: positionAsNumber,
                          activeStep: {
                            ...toUIStep(connection),
                            icon: getConnectionIcon(
                              process.env.PUBLIC_URL,
                              connection
                            ),
                          },
                          steps: toUIStepCollection(
                            getSteps(integration, flowId)
                          ),
                        })}
                        content={
                          <IntegrationEditorChooseAction>
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
                                      href={this.props.selectHref(
                                        a.id!,
                                        { connectionId, flowId, position },
                                        { connection, integration }
                                      )}
                                    >
                                      Select
                                    </ButtonLink>
                                  }
                                />
                              ))}
                          </IntegrationEditorChooseAction>
                        }
                        cancelHref={this.props.cancelHref(
                          { connectionId, flowId, position },
                          { connection, integration }
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
