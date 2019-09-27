import {
  createConditionalFlow,
  FlowKind,
  getFlow,
  getOutputDataShapeFromPreviousStep,
  getStep,
  getSteps,
  reconcileConditionalFlows,
  WithConnection,
  WithFilterOptions,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration, StepKind, StringMap } from '@syndesis/models';
import {
  ChoiceCardHeader,
  ChoicePageCard,
  IntegrationEditorLayout,
  PageLoader,
  PageSection,
} from '@syndesis/ui';
import { key, WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { ApiError, PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  IChoiceStepRouteParams,
  IChoiceStepRouteState,
  IPageWithEditorBreadcrumb,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import { IChoiceFormConfiguration } from './interfaces';
import { createChoiceConfiguration, getFlowDescription } from './utils';
import { WithChoiceConfigurationForm } from './WithChoiceConfigurationForm';

const NEW_CONDITIONAL_FLOW_NAME = 'Conditional';
const DEFAULT_FLOW_NAME = 'Default';
const DEFAULT_FLOW_DESCRIPTION = 'Use this as default';

export interface IChoiceStepPageProps extends IPageWithEditorBreadcrumb {
  mode: 'adding' | 'editing';
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  backHref: (
    p: IChoiceStepRouteParams,
    s: IChoiceStepRouteState
  ) => H.LocationDescriptor;
  cancelHref: (
    p: IChoiceStepRouteParams,
    s: IChoiceStepRouteState
  ) => H.LocationDescriptor;
  postConfigureHref: (
    integration: Integration,
    p: IChoiceStepRouteParams,
    s: IChoiceStepRouteState
  ) => H.LocationDescriptorObject;
}

export class ChoiceStepPage extends React.Component<IChoiceStepPageProps> {
  public render() {
    return (
      <WithConnection id={'flow'}>
        {({ data, error, errorMessage, hasData }) => (
          <WithIntegrationHelpers>
            {({ addStep, updateStep }) => (
              <WithRouteData<IChoiceStepRouteParams, IChoiceStepRouteState>>
                {(params, state, { history }) => {
                  const positionAsNumber = parseInt(params.position, 10);
                  const dataShape = getOutputDataShapeFromPreviousStep(state.integration, params.flowId, positionAsNumber);
                  const step = state.step;
                  // parse the configured properties
                  const configuration = createChoiceConfiguration(
                    step.configuredProperties || {}
                  );
                  // create the values displayed in the form
                  const initialFormValue = {
                    defaultFlowId: configuration.defaultFlowEnabled
                      ? configuration.defaultFlow!
                      : '',
                    flowConditions: configuration.flows.map(
                      ({ condition, flow, op, path, value }) => ({
                        condition,
                        flowId: flow,
                        op,
                        path,
                        value,
                      })
                    ),
                    routingScheme: configuration.routingScheme,
                    useDefaultFlow: configuration.defaultFlowEnabled,
                  };
                  const hasConfiguration = (values: IChoiceFormConfiguration) => {
                    return values.flowConditions.some(option => (option.condition! || option.path! || option.value!).length > 0);
                  };
                  const onUpdatedIntegration = async (
                    values: IChoiceFormConfiguration
                  ) => {
                    const {
                      action,
                      description,
                      id,
                      metadata,
                      name,
                      properties,
                      stepKind,
                    } = step;
                    // Construct a new step object
                    const updatedStep = {
                      action,
                      description,
                      id: id || key(),
                      metadata,
                      name,
                      properties,
                      stepKind,
                    };
                    const flowCollection = values.flowConditions.map(
                      flowCondition => {
                        // Create a flow for new conditions or grab the
                        // existing flow if we're working with an existing
                        // configuration.  Ensure that if there's a flow
                        // ID set and we can't find one, create a new one
                        const flow =
                          typeof flowCondition.flowId === 'undefined'
                            ? createConditionalFlow(
                                NEW_CONDITIONAL_FLOW_NAME,
                                getFlowDescription(flowCondition),
                                FlowKind.CONDITIONAL,
                                params.flowId,
                                data,
                                updatedStep
                              )
                            : getFlow(
                                state.updatedIntegration || state.integration,
                                flowCondition.flowId
                              ) ||
                              createConditionalFlow(
                                NEW_CONDITIONAL_FLOW_NAME,
                                getFlowDescription(flowCondition),
                                FlowKind.CONDITIONAL,
                                params.flowId,
                                data,
                                updatedStep,
                                flowCondition.flowId
                              )!;
                        // update the description
                        flow.description = getFlowDescription(flowCondition);
                        return {
                          condition: flowCondition.condition,
                          flow,
                          flowId: flow.id,
                          op: flowCondition.op,
                          path: flowCondition.path,
                          value: flowCondition.value,
                        };
                      }
                    );
                    const defaultFlow = values.useDefaultFlow
                      ? values.defaultFlowId === ''
                        ? createConditionalFlow(
                            DEFAULT_FLOW_NAME,
                            DEFAULT_FLOW_DESCRIPTION,
                            FlowKind.DEFAULT,
                            params.flowId,
                            data,
                            updatedStep
                          )
                        : getFlow(
                            state.updatedIntegration || state.integration,
                            values.defaultFlowId
                          )
                      : undefined;
                    const configuredProperties: StringMap<string> = {
                      flows: JSON.stringify(
                        flowCollection.map(f => ({
                          condition: f.condition,
                          flow: f.flowId,
                          op: f.op,
                          path: f.path,
                          value: f.value,
                        }))
                      ),
                      routingScheme: values.routingScheme,
                    };
                    const updatedFlows = flowCollection.map(f => f.flow);
                    if (typeof defaultFlow !== 'undefined') {
                      configuredProperties.default = defaultFlow!.id!;
                      updatedFlows.push(defaultFlow);
                    }
                    const updatedIntegration = await (this.props.mode ===
                      'adding'
                      ? addStep
                      : updateStep)(
                      state.updatedIntegration || state.integration,
                      updatedStep,
                      params.flowId,
                      positionAsNumber,
                      configuredProperties
                    );
                    const stepWithUpdatedDescriptor = getStep(
                      updatedIntegration,
                      params.flowId,
                      positionAsNumber
                    )! as StepKind;
                    const reconciledIntegration = reconcileConditionalFlows(
                      updatedIntegration,
                      updatedFlows,
                      stepWithUpdatedDescriptor.id!,
                      stepWithUpdatedDescriptor.action!.descriptor!
                        .inputDataShape!,
                      stepWithUpdatedDescriptor.action!.descriptor!
                        .outputDataShape!
                    );
                    history.push(
                      this.props.postConfigureHref(
                        reconciledIntegration,
                        {
                          ...params,
                        } as IChoiceStepRouteParams,
                        {
                          ...state,
                          step: stepWithUpdatedDescriptor,
                          updatedIntegration: reconciledIntegration
                        } as IChoiceStepRouteState
                      )
                    );
                  };
                  return (
                    <>
                      <PageTitle title={'Configure Conditional Flows'} />
                      <IntegrationEditorLayout
                        title={'Configure Conditional Flows'}
                        description={
                          'Define one to many conditions in order to route messages to different flows.'
                        }
                        toolbar={this.props.getBreadcrumb(
                          'Configure Conditional Flows',
                          params,
                          state
                        )}
                        sidebar={this.props.sidebar({
                          activeIndex: positionAsNumber,
                          activeStep: toUIStep(state.step),
                          steps: toUIStepCollection(
                            getSteps(
                              state.updatedIntegration || state.integration,
                              params.flowId
                            )
                          ),
                        })}
                        content={
                          <WithFilterOptions dataShape={dataShape}>
                            {({ data: options, error: optionsError, errorMessage: optionsErrorMessage, hasData: hasOptions }) => (
                              <WithLoader
                                error={optionsError}
                                loading={!hasOptions}
                                loaderChildren={<PageLoader />}
                                errorChildren={
                                  <PageSection>
                                    <ApiError error={optionsErrorMessage!} />
                                  </PageSection>
                                }
                              >
                                {() => (
                                  <WithChoiceConfigurationForm
                                    configMode={params.configMode}
                                    initialValue={initialFormValue}
                                    filterOptions={options}
                                    onUpdatedIntegration={onUpdatedIntegration}
                                    stepId={step.id!}
                                  >
                                    {({ fields, isValid, submitForm, values }) => (
                                      <ChoicePageCard
                                        backHref={this.props.backHref(params, state)}
                                        header={
                                          <ChoiceCardHeader
                                            i18nConditions={'Conditions'}
                                          />
                                        }
                                        i18nBack={'Choose Action'}
                                        i18nDone={'Next'}
                                        isBackAllowed={!hasConfiguration(values)}
                                        isValid={isValid}
                                        submitForm={submitForm}
                                      >
                                          {fields}
                                      </ChoicePageCard>
                                    )}
                                  </WithChoiceConfigurationForm>
                                )}
                              </WithLoader>
                            )}
                          </WithFilterOptions>
                        }
                        cancelHref={this.props.cancelHref(params, state)}
                      />
                    </>
                  );
                }}
              </WithRouteData>
            )}
          </WithIntegrationHelpers>
        )}
      </WithConnection>
    );
  }
}
