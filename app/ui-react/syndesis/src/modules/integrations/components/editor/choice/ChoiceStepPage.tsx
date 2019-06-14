import {
  createConditionalFlow,
  FlowKind,
  getFlow,
  getStep,
  getSteps,
  reconcileConditionalFlows,
  WithConnection,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration, StringMap } from '@syndesis/models';
import {
  ChoiceCardHeader,
  EditorPageCard,
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
import { createChoiceConfiguration } from './utils';
import { WithChoiceConfigurationForm } from './WithChoiceConfigurationForm';

const NEW_CONDITIONAL_FLOW_NAME = 'Conditional';
const DEFAULT_FLOW_NAME = 'Default';
const DEFAULT_FLOW_DESCRIPTION = 'Use this as default';

export interface IChoiceStepPageProps extends IPageWithEditorBreadcrumb {
  mode: 'adding' | 'editing';
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  cancelHref: (
    p: IChoiceStepRouteParams,
    s: IChoiceStepRouteState
  ) => H.LocationDescriptor;
  // tslint:disable-next-line:react-unused-props-and-state
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
        {({ data, error, hasData }) => (
          <WithIntegrationHelpers>
            {({ addStep, updateStep }) => (
              <WithRouteData<IChoiceStepRouteParams, IChoiceStepRouteState>>
                {(params, state, { history }) => {
                  const positionAsNumber = parseInt(params.position, 10);
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
                      ({ condition, flow }) => ({
                        condition,
                        flowId: flow,
                      })
                    ),
                    routingScheme: configuration.routingScheme,
                    useDefaultFlow: configuration.defaultFlowEnabled,
                  };
                  const onUpdatedIntegration = async (
                    values: IChoiceFormConfiguration
                  ) => {
                    const updatedStep = {
                      ...step,
                      id: step.id || key(),
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
                                flowCondition.condition,
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
                                flowCondition.condition,
                                FlowKind.CONDITIONAL,
                                params.flowId,
                                data,
                                updatedStep,
                                flowCondition.flowId
                              )!;
                        // update the description
                        flow.description = flowCondition.condition;
                        return {
                          condition: flowCondition.condition,
                          flow,
                          flowId: flow.id,
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
                            state.step
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
                    )!;
                    const reconciledIntegration = reconcileConditionalFlows(
                      updatedIntegration,
                      updatedFlows,
                      stepWithUpdatedDescriptor.id!,
                      stepWithUpdatedDescriptor.action!.descriptor!
                    );
                    history.push(
                      this.props.postConfigureHref(updatedIntegration, params, {
                        ...state,
                        updatedIntegration: reconciledIntegration,
                      })
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
                          <WithLoader
                            error={error}
                            loading={!hasData}
                            loaderChildren={<PageLoader />}
                            errorChildren={
                              <PageSection>
                                <ApiError />
                              </PageSection>
                            }
                          >
                            {() => (
                              <WithChoiceConfigurationForm
                                initialValue={initialFormValue}
                                onUpdatedIntegration={onUpdatedIntegration}
                                stepId={step.id!}
                              >
                                {({ fields, isValid, submitForm }) => (
                                  <EditorPageCard
                                    header={
                                      <ChoiceCardHeader
                                        i18nConditions={'Conditions'}
                                      />
                                    }
                                    i18nDone={'Done'}
                                    isValid={isValid}
                                    submitForm={submitForm}
                                  >
                                    {fields}
                                  </EditorPageCard>
                                )}
                              </WithChoiceConfigurationForm>
                            )}
                          </WithLoader>
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
