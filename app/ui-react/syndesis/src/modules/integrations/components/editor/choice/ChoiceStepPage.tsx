import {
  createConditionalFlow,
  FlowKind,
  getFlow,
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
import { WithLoader, WithRouteData } from '@syndesis/utils';
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
                    const flowCollection = values.flowConditions.map(
                      flowCondition => {
                        const flow =
                          typeof flowCondition.flowId === 'undefined'
                            ? createConditionalFlow(
                                'Conditional',
                                flowCondition.condition,
                                FlowKind.CONDITIONAL,
                                params.flowId,
                                data,
                                state.step
                              )
                            : getFlow(
                                state.updatedIntegration || state.integration,
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
                            'Default',
                            'Use this as default',
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
                    const defaultFlowId =
                      typeof defaultFlow !== 'undefined' ? defaultFlow.id! : '';
                    const updatedIntegration = reconcileConditionalFlows(
                      await (this.props.mode === 'adding'
                        ? addStep
                        : updateStep)(
                        state.updatedIntegration || state.integration,
                        state.step,
                        params.flowId,
                        positionAsNumber,
                        configuredProperties
                      ),
                      updatedFlows,
                      flowCollection.map(f => f.flowId!),
                      defaultFlowId,
                      state.step.id!
                    );
                    history.push(
                      this.props.postConfigureHref(updatedIntegration, params, {
                        ...state,
                        updatedIntegration,
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
