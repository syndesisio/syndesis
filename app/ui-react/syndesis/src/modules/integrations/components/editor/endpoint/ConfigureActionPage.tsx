import {
  getStep,
  getSteps,
  isEndStep,
  isStartStep,
  requiresInputDescribeDataShape,
  requiresOutputDescribeDataShape,
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
  IPageWithEditorBreadcrumb,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import {
  IOnUpdatedIntegrationProps,
  WithConfigurationForm,
} from './WithConfigurationForm';

export interface IConfigureActionPageProps extends IPageWithEditorBreadcrumb {
  backHref: (
    p: IConfigureActionRouteParams,
    s: IConfigureActionRouteState
  ) => H.LocationDescriptor;
  cancelHref: (
    p: IConfigureActionRouteParams,
    s: IConfigureActionRouteState
  ) => H.LocationDescriptor;
  mode: 'adding' | 'editing';
  nextPageHref: (
    p: IConfigureActionRouteParams,
    s: IConfigureActionRouteState
  ) => H.LocationDescriptorObject;
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  postConfigureHref: (
    requiresDataShape: boolean,
    integration: Integration,
    p: IConfigureActionRouteParams | IDescribeDataShapeRouteParams,
    s: IConfigureActionRouteState | IDescribeDataShapeRouteState
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
            {(params, state, { history }) => {
              const pageAsNumber = parseInt(params.page, 10);
              const positionAsNumber = parseInt(params.position, 10);
              const oldStepConfig = getStep(
                state.integration,
                params.flowId,
                positionAsNumber
              );
              /**
               * configured properties will be set in the route state for
               * configuration pages higher than 0. If it's not set, its value
               * depends on the mode: for `adding` there is no initial value,
               * for `editing` we can fetch it from the old step config object.
               */
              const configuredProperties =
                state.configuredProperties ||
                (this.props.mode === 'editing' && oldStepConfig
                  ? oldStepConfig.configuredProperties
                  : {});
              const onUpdatedIntegration = async ({
                action,
                moreConfigurationSteps,
                values,
              }: IOnUpdatedIntegrationProps) => {
                const updatedIntegration = await (this.props.mode ===
                  'adding' && pageAsNumber === 0
                  ? addConnection
                  : updateConnection)(
                  state.updatedIntegration || state.integration,
                  state.connection,
                  action,
                  params.flowId,
                  positionAsNumber,
                  values
                );
                if (moreConfigurationSteps) {
                  history.push(
                    this.props.nextPageHref(
                      {
                        ...params,
                        page: `${pageAsNumber + 1}`,
                      },
                      {
                        ...state,
                        configuredProperties: {
                          ...values,
                          ...configuredProperties,
                        },
                        updatedIntegration,
                      }
                    )
                  );
                } else {
                  const stepKind = getStep(
                    updatedIntegration,
                    params.flowId,
                    positionAsNumber
                  ) as StepKind;
                  const gotoDescribeData = (direction: DataShapeDirection) => {
                    history.push(
                      this.props.postConfigureHref(
                        true,
                        updatedIntegration!,
                        {
                          ...params,
                          direction,
                        },
                        {
                          connection: state.connection,
                          integration: state.integration,
                          step: stepKind,
                          updatedIntegration,
                        }
                      )
                    );
                  };
                  const gotoDefaultNextPage = () => {
                    history.push(
                      this.props.postConfigureHref(
                        false,
                        updatedIntegration!,
                        params,
                        {
                          ...state,
                          updatedIntegration,
                        }
                      )
                    );
                  };
                  const descriptor = stepKind.action!.descriptor!;
                  if (
                    isStartStep(
                      state.integration,
                      params.flowId,
                      positionAsNumber
                    )
                  ) {
                    if (requiresOutputDescribeDataShape(descriptor)) {
                      gotoDescribeData(DataShapeDirection.OUTPUT);
                    } else {
                      gotoDefaultNextPage();
                    }
                  } else if (
                    isEndStep(
                      state.integration,
                      params.flowId,
                      positionAsNumber
                    )
                  ) {
                    if (requiresInputDescribeDataShape(descriptor)) {
                      gotoDescribeData(DataShapeDirection.INPUT);
                    } else {
                      gotoDefaultNextPage();
                    }
                  } else {
                    if (requiresInputDescribeDataShape(descriptor)) {
                      gotoDescribeData(DataShapeDirection.INPUT);
                    } else if (requiresOutputDescribeDataShape(descriptor)) {
                      gotoDescribeData(DataShapeDirection.OUTPUT);
                    } else {
                      gotoDefaultNextPage();
                    }
                  }
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
                    toolbar={this.props.getBreadcrumb(
                      'Configure the action',
                      params,
                      state
                    )}
                    sidebar={this.props.sidebar({
                      activeIndex: positionAsNumber,
                      activeStep: {
                        ...toUIStep(state.connection),
                      },
                      steps: toUIStepCollection(
                        getSteps(state.integration, params.flowId)
                      ),
                    })}
                    content={
                      <WithConfigurationForm
                        key={`${positionAsNumber}:${pageAsNumber}`}
                        connection={state.connection}
                        actionId={params.actionId}
                        oldAction={
                          oldStepConfig && oldStepConfig.action
                            ? oldStepConfig!.action!
                            : undefined
                        }
                        configurationPage={pageAsNumber}
                        initialValue={configuredProperties}
                        onUpdatedIntegration={onUpdatedIntegration}
                        chooseActionHref={this.props.backHref(params, state)}
                      />
                    }
                    cancelHref={this.props.cancelHref(params, state)}
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
