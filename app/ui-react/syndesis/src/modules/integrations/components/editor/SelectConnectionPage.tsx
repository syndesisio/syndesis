import {
  getEmptyIntegration,
  getSteps,
  visibleStepsByPosition,
  WithConnections,
  WithExtensions,
  WithSteps,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Step, StepKind } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionsGridCell,
  IntegrationEditorChooseConnection,
  IntegrationEditorLayout,
  IntegrationsListSkeleton,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { ApiError, PageTitle } from '../../../../shared';
import resolvers from '../../../resolvers';
import {
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  IUIStep,
} from './interfaces';
import { getStepHref, IGetStepHrefs, toStepKindCollection } from './utils';

export interface ISelectConnectionPageProps extends IGetStepHrefs {
  cancelHref: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  sidebar: (props: { steps: Step[]; activeIndex: number }) => React.ReactNode;
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
        {(params, state) => {
          const { flowId, position } = params;
          const { integration = getEmptyIntegration() } = state;
          const positionAsNumber = parseInt(position, 10) || 0;
          const integrationSteps = getSteps(integration, flowId);
          return (
            <>
              <PageTitle title={'Choose a connection'} />
              <IntegrationEditorLayout
                sidebar={this.props.sidebar({
                  activeIndex: positionAsNumber,
                  steps: integrationSteps,
                })}
                content={
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
                            {({ items: steps }) => (
                              <IntegrationEditorChooseConnection
                                i18nTitle={'Choose a connection'}
                                i18nSubtitle={
                                  'Click the connection that completes the integration. If the connection you need is not available, click Create Connection.'
                                }
                              >
                                <WithLoader
                                  error={connectionsError || extensionsError}
                                  loading={
                                    !hasConnectionsData || !hasExtensionsData
                                  }
                                  loaderChildren={<IntegrationsListSkeleton />}
                                  errorChildren={<ApiError />}
                                >
                                  {() => {
                                    const stepKinds = toStepKindCollection(
                                      positionAsNumber === 0
                                        ? connectionsData.connectionsWithFromAction
                                        : connectionsData.connectionsWithToAction,
                                      extensionsData.items,
                                      steps
                                    );
                                    const visibleSteps = visibleStepsByPosition(
                                      stepKinds as StepKind[],
                                      positionAsNumber
                                    ) as IUIStep[];
                                    return (
                                      <>
                                        {visibleSteps.map(
                                          (step, idx: number) => (
                                            <ConnectionsGridCell key={idx}>
                                              <ConnectionCard
                                                name={step.name}
                                                description={
                                                  step.description || ''
                                                }
                                                icon={step.icon}
                                                href={getStepHref(
                                                  step,
                                                  params,
                                                  state,
                                                  this.props
                                                )}
                                              />
                                            </ConnectionsGridCell>
                                          )
                                        )}
                                        <ConnectionsGridCell>
                                          <ConnectionCard
                                            name={'Create connection'}
                                            description={''}
                                            icon={''}
                                            href={resolvers.connections.create.selectConnector()}
                                          />
                                        </ConnectionsGridCell>
                                      </>
                                    );
                                  }}
                                </WithLoader>
                              </IntegrationEditorChooseConnection>
                            )}
                          </WithSteps>
                        )}
                      </WithExtensions>
                    )}
                  </WithConnections>
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
