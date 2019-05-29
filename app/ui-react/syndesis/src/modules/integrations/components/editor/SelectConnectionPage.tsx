import {
  getEmptyIntegration,
  getSteps,
  WithConnections,
  WithExtensions,
  WithSteps,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { StepKind } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import { ConnectionsWithToolbar } from '../../../connections/components';
import { IEditorSidebarProps } from './EditorSidebar';
import {
  IPageWithEditorBreadcrumb,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  IUIStep,
} from './interfaces';
import {
  getStepHref,
  IGetStepHrefs,
  mergeConnectionsSources,
  toUIStepCollection,
  visibleStepsByPosition,
} from './utils';

export interface ISelectConnectionPageProps
  extends IGetStepHrefs,
    IPageWithEditorBreadcrumb {
  cancelHref: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
}

/**
 * This page shows the list of connections containing actions with a **to**
 * pattern.
 *
 * This component expects some [params]{@link ISelectConnectionRouteParams} and
 * [state]{@link ISelectConnectionRouteState} to be properly set in the route
 * object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export class SelectConnectionPage extends React.Component<
  ISelectConnectionPageProps
> {
  public render() {
    return (
      <WithRouteData<ISelectConnectionRouteParams, ISelectConnectionRouteState>>
        {(params, state, { history }) => {
          const { flowId, position } = params;
          const { integration = getEmptyIntegration() } = state;
          const positionAsNumber = parseInt(position, 10) || 0;
          const integrationSteps = getSteps(integration, flowId);
          return (
            <>
              <PageTitle title={'Choose a connection'} />
              <IntegrationEditorLayout
                title={'Choose a connection'}
                description={
                  'Click the connection that completes the integration. If the connection you need is not available, click Create Connection.'
                }
                toolbar={this.props.getBreadcrumb(
                  'Choose a connection',
                  params,
                  state
                )}
                sidebar={this.props.sidebar({
                  activeIndex: positionAsNumber,
                  steps: toUIStepCollection(integrationSteps),
                })}
                content={
                  <Translation ns={['connections', 'shared']}>
                    {t => (
                      <WithConnections>
                        {({
                          data: connectionsData,
                          hasData: hasConnectionsData,
                          error: connectionsError,
                        }) => (
                          <WithExtensions>
                            {({
                              data: extensionsData,
                              hasData: hasExtensionsData,
                              error: extensionsError,
                            }) => (
                              <WithSteps>
                                {({ items: steps }) => {
                                  const stepKinds = mergeConnectionsSources(
                                    connectionsData.dangerouslyUnfilteredConnections,
                                    extensionsData.items,
                                    steps
                                  );
                                  const visibleSteps = visibleStepsByPosition(
                                    stepKinds as StepKind[],
                                    positionAsNumber,
                                    integrationSteps
                                  ) as IUIStep[];
                                  return (
                                    <ConnectionsWithToolbar
                                      loading={
                                        !hasConnectionsData ||
                                        !hasExtensionsData
                                      }
                                      error={
                                        connectionsError || extensionsError
                                      }
                                      includeConnectionMenu={false}
                                      getConnectionHref={step =>
                                        getStepHref(
                                          step,
                                          params,
                                          state,
                                          this.props
                                        )
                                      }
                                      connections={visibleSteps}
                                      createConnectionButtonStyle={'default'}
                                    />
                                  );
                                }}
                              </WithSteps>
                            )}
                          </WithExtensions>
                        )}
                      </WithConnections>
                    )}
                  </Translation>
                }
                cancelHref={this.props.cancelHref(params, state)}
              />
            </>
          );
        }}
      </WithRouteData>
    );
  }
}
