import {
  getEmptyIntegration,
  getSteps,
  visibleStepsByPosition,
  WithConnections,
  WithExtensions,
  WithSteps,
} from '@syndesis/api';
import { Step, StepKind } from '@syndesis/models';
import {
  ButtonLink,
  IntegrationEditorChooseConnection,
  IntegrationEditorConnectionsListItem,
  IntegrationEditorLayout,
  IntegrationsListSkeleton,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as H from 'history';
import * as React from 'react';
import { ApiError, PageTitle } from '../../../../shared';
import {
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  IUIStep,
} from './interfaces';
import { getStepHref, IGetStepHrefs, toStepKindCollection } from './utils';

export interface ISelectConnectionPageProps extends IGetStepHrefs {
  backHref?: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  cancelHref: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  header: React.ReactNode;
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
                header={this.props.header}
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
                                            <IntegrationEditorConnectionsListItem
                                              key={idx}
                                              integrationName={step.name}
                                              integrationDescription={
                                                step.description ||
                                                'No description available.'
                                              }
                                              icon={
                                                <img
                                                  src={step.icon}
                                                  width={24}
                                                  height={24}
                                                />
                                              }
                                              actions={
                                                <ButtonLink
                                                  href={getStepHref(
                                                    step,
                                                    params,
                                                    state,
                                                    this.props
                                                  )}
                                                >
                                                  Select
                                                </ButtonLink>
                                              }
                                            />
                                          )
                                        )}
                                        <IntegrationEditorConnectionsListItem
                                          integrationName={''}
                                          integrationDescription={''}
                                          icon={''}
                                          actions={
                                            <ButtonLink href={'#'}>
                                              Create connection
                                            </ButtonLink>
                                          }
                                        />
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
                backHref={
                  this.props.backHref
                    ? this.props.backHref(params, state)
                    : undefined
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
