import { getSteps, WithConnection } from '@syndesis/api';
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
  IPageWithEditorBreadcrumb,
  ISelectActionRouteParams,
  ISelectActionRouteState,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';

export interface ISelectActionPageProps extends IPageWithEditorBreadcrumb {
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
        {(params, state) => {
          const positionAsNumber = parseInt(params.position, 10);
          return (
            <WithConnection
              id={params.connectionId}
              initialValue={state.connection}
            >
              {({ data, hasData, error, errorMessage, loading }) => (
                <WithLoader
                  error={error}
                  loading={loading && !hasData}
                  loaderChildren={<PageLoader />}
                  errorChildren={<ApiError error={errorMessage!} />}
                >
                  {() => {
                    const steps = toUIStepCollection(
                      getSteps(state.integration, params.flowId)
                    );
                    // if we're looking at the 1st step, only show
                    // actions with 'From'.  If we're looking at the
                    // last step, only show actions with 'To' or 'PollEnrich'.
                    // Otherwise, show actions with 'To', 'Pipe' or 'PollEnrich'.
                    const actions =
                      positionAsNumber > 0
                        ? positionAsNumber === steps.length
                          ? [
                              ...data.actionsWithTo,
                              ...data.actionsWithPollEnrich,
                            ]
                          : [
                              ...data.actionsWithTo,
                              ...data.actionsWithPipe,
                              ...data.actionsWithPollEnrich,
                            ]
                        : data.actionsWithFrom;
                    return (
                      <>
                        <PageTitle title={'Choose an action'} />
                        <IntegrationEditorLayout
                          title={'Choose an action'}
                          description={
                            'Choose an action for the selected connection.'
                          }
                          toolbar={this.props.getBreadcrumb(
                            'Choose an action',
                            params,
                            state
                          )}
                          sidebar={this.props.sidebar({
                            activeIndex: positionAsNumber,
                            activeStep: {
                              ...toUIStep(state.connection),
                            },
                            steps,
                          })}
                          content={
                            <IntegrationEditorChooseAction>
                              {actions
                                .sort((a, b) => a.name.localeCompare(b.name))
                                .map((a, idx) => (
                                  <IntegrationEditorActionsListItem
                                    key={idx}
                                    name={a.name}
                                    description={
                                      a.description ||
                                      'No description available.'
                                    }
                                    actions={
                                      <ButtonLink
                                        data-testid={
                                          'select-action-page-select-button'
                                        }
                                        href={this.props.selectHref(
                                          a.id!,
                                          params,
                                          state
                                        )}
                                      >
                                        Select
                                      </ButtonLink>
                                    }
                                  />
                                ))}
                            </IntegrationEditorChooseAction>
                          }
                          cancelHref={this.props.cancelHref(params, state)}
                        />
                      </>
                    );
                  }}
                </WithLoader>
              )}
            </WithConnection>
          );
        }}
      </WithRouteData>
    );
  }
}
