import {
  getConnectionIcon,
  getStep,
  getSteps,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration, StepKind } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  DataShapeDirection,
  IConfigureActionRouteParams,
  IConfigureActionRouteState,
  IDescribeDataShapeRouteParams,
  IDescribeDataShapeRouteState,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import {
  IOnUpdatedIntegrationProps,
  WithConfigurationForm,
} from './WithConfigurationForm';

export interface IConfigureActionPageProps {
  backHref: (
    p: IConfigureActionRouteParams,
    s: IConfigureActionRouteState
  ) => H.LocationDescriptor;
  cancelHref: (
    p: IConfigureActionRouteParams,
    s: IConfigureActionRouteState
  ) => H.LocationDescriptor;
  mode: 'adding' | 'editing';
  nextStepHref: (
    p: IConfigureActionRouteParams,
    s: IConfigureActionRouteState
  ) => H.LocationDescriptorObject;
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  postConfigureHref: (
    integration: Integration,
    p: IDescribeDataShapeRouteParams,
    s: IDescribeDataShapeRouteState
  ) => H.LocationDescriptorObject;
}

/**
 * This page shows the configuration form for a given action.
 *
 * Submitting the form will update an *existing* integration step in
 * the [position specified in the params]{@link IConfigureActionRouteParams#position}
 * of the first flow, set up as specified by the form values.
 *
 * This component expects some [url params]{@link IConfigureActionRouteParams}
 * and [state]{@link IConfigureActionRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export class ConfigureActionPage extends React.Component<
  IConfigureActionPageProps
> {
  public render() {
    return (
      <WithIntegrationHelpers>
        {({ addConnection, updateConnection }) => (
          <WithRouteData<
            IConfigureActionRouteParams,
            IConfigureActionRouteState
          >>
            {(
              { actionId, flowId, step = '0', position },
              {
                configuredProperties,
                connection,
                integration,
                updatedIntegration,
              },
              { history }
            ) => {
              const stepAsNumber = parseInt(step, 10);
              const positionAsNumber = parseInt(position, 10);
              const onUpdatedIntegration = async ({
                action,
                moreConfigurationSteps,
                values,
              }: IOnUpdatedIntegrationProps) => {
                updatedIntegration = await (this.props.mode === 'adding' &&
                  stepAsNumber === 0
                  ? addConnection
                  : updateConnection)(
                  updatedIntegration || integration,
                  connection,
                  action,
                  flowId,
                  positionAsNumber,
                  values
                );
                if (moreConfigurationSteps) {
                  history.push(
                    this.props.nextStepHref(
                      {
                        actionId,
                        flowId,
                        position,
                        step: `${stepAsNumber + 1}`,
                      },
                      {
                        configuredProperties,
                        connection,
                        integration,
                        updatedIntegration,
                      }
                    )
                  );
                } else {
                  history.push(
                    this.props.postConfigureHref(
                      updatedIntegration,
                      { direction: DataShapeDirection.INPUT, flowId, position },
                      {
                        integration,
                        step: getStep(
                          updatedIntegration,
                          flowId,
                          positionAsNumber
                        ) as StepKind,
                        updatedIntegration,
                      }
                    )
                  );
                }
              };

              return (
                <>
                  <PageTitle title={'Configure the action'} />
                  <IntegrationEditorLayout
                    title={'Configure the action'}
                    description={
                      'Fill in the required information for the selected action.'
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
                      steps: toUIStepCollection(getSteps(integration, flowId)),
                    })}
                    content={
                      <WithConfigurationForm
                        connection={connection}
                        actionId={actionId}
                        configurationStep={stepAsNumber}
                        initialValue={configuredProperties}
                        onUpdatedIntegration={onUpdatedIntegration}
                        chooseActionHref={this.props.backHref(
                          { actionId, flowId, step, position },
                          {
                            configuredProperties,
                            connection,
                            integration,
                            updatedIntegration,
                          }
                        )}
                      />
                    }
                    cancelHref={this.props.cancelHref(
                      { actionId, flowId, step, position },
                      {
                        configuredProperties,
                        connection,
                        integration,
                        updatedIntegration,
                      }
                    )}
                  />
                </>
              );
            }}
          </WithRouteData>
        )}
      </WithIntegrationHelpers>
    );
  }
}
