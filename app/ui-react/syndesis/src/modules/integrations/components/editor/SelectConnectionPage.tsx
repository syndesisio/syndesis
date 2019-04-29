import {
  getConnectionIcon,
  getEmptyIntegration,
  getExtensionIcon,
  getStepKindIcon,
  getSteps,
  visibleStepsByPosition,
  WithConnections,
  WithExtensions,
  WithSteps,
} from '@syndesis/api';
import {
  ConnectionOverview,
  Extension,
  Step,
  StepKind,
} from '@syndesis/models';
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
} from './interfaces';

export interface IUIStep extends StepKind {
  icon: string;
}

export function toStepKindCollection(
  connections: ConnectionOverview[],
  extensions: Extension[],
  steps: StepKind[]
): IUIStep[] {
  return [
    ...connections.map(c => ({
      ...c,
      description: c.description || '',
      icon: getConnectionIcon(process.env.PUBLIC_URL, c),
      properties: undefined,
    })),
    ...extensions.reduce(
      (extentionsByAction, extension) => {
        extension.actions.forEach(a => {
          let properties = {};
          if (
            a.descriptor &&
            Array.isArray(a.descriptor.propertyDefinitionSteps)
          ) {
            properties = a.descriptor.propertyDefinitionSteps.reduce(
              (acc, current) => {
                return { ...acc, ...current.properties };
              },
              {}
            );
          }
          if (a.actionType === 'step') {
            extentionsByAction.push({
              action: a,
              configuredProperties: undefined,
              description: a.description || '',
              extension,
              icon: `${process.env.PUBLIC_URL}${getExtensionIcon(extension)}`,
              name: a.name,
              properties,
              stepKind: 'extension',
            });
          }
        });
        return extentionsByAction;
      },
      [] as IUIStep[]
    ),
    ...steps.map(s => ({
      ...s,
      icon: `${process.env.PUBLIC_URL}${getStepKindIcon(s.stepKind)}`,
    })),
  ].sort((a, b) => a.name.localeCompare(b.name));
}

export const getStepKind = (stepOrConnection: ConnectionOverview | Step) => {
  if ((stepOrConnection as ConnectionOverview).connectorId === 'api-provider') {
    return 'api-provider';
  }
  if ((stepOrConnection as Step).stepKind) {
    // not a connection
  }
  return 'endpoint';
};

export interface ISelectConnectionPageProps {
  backHref?: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  cancelHref: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  header: React.ReactNode;
  apiProviderHref: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptorObject;
  connectionHref: (
    connection: ConnectionOverview,
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptorObject;
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
        {(params, state, { history }) => {
          const { flowId, position } = params;
          const { integration = getEmptyIntegration() } = state;
          const positionAsNumber = parseInt(position, 10) || 0;
          const onStepClick = (connectionOrStep: ConnectionOverview | Step) => {
            const stepKind = getStepKind(connectionOrStep);
            switch (stepKind) {
              case 'api-provider':
                history.push(this.props.apiProviderHref(params, state));
                break;
              case 'endpoint':
                history.push(
                  this.props.connectionHref(
                    connectionOrStep as ConnectionOverview,
                    {
                      ...params,
                    },
                    state
                  )
                );
                break;
            }
          };
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
                                    !hasConnectionsData && !hasExtensionsData
                                  }
                                  loaderChildren={<IntegrationsListSkeleton />}
                                  errorChildren={<ApiError />}
                                >
                                  {() => (
                                    <>
                                      {(visibleStepsByPosition(
                                        integration,
                                        flowId,
                                        toStepKindCollection(
                                          positionAsNumber === 0
                                            ? connectionsData.connectionsWithFromAction
                                            : connectionsData.connectionsWithToAction,
                                          extensionsData.items,
                                          steps
                                        ),
                                        positionAsNumber
                                      ) as IUIStep[]).map(
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
                                                onClick={() =>
                                                  onStepClick(step)
                                                }
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
                                  )}
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
