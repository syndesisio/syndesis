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
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../shared';
import { ConnectionsWithToolbar } from '../../../connections/components';
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
                integrationName={integration.name}
                integrationDescription={integration.description}
                i18nIntegrationNamePlaceholder={'Click to set a name'}
                i18nIntegrationDescriptionPlaceholder={
                  'Click to set a description'
                }
                onIntegrationNameChange={() => Promise.resolve(true)}
                onIntegrationDescriptionChange={() => Promise.resolve(true)}
                title={'Choose a connection'}
                description={
                  'Click the connection that completes the integration. If the connection you need is not available, click Create Connection.'
                }
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
                            {({ items: steps }) => {
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
                                <ConnectionsWithToolbar
                                  loading={
                                    !hasConnectionsData || !hasExtensionsData
                                  }
                                  error={connectionsError || extensionsError}
                                  includeConnectionMenu={false}
                                  getConnectionHref={step =>
                                    getStepHref(step, params, state, this.props)
                                  }
                                  connections={visibleSteps}
                                />
                              );
                            }}
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
