/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {
  getChoiceConfigMode,
  getFlow,
  getStep,
  getSteps,
  reconcileConditionalFlows,
  toDataShapeKinds,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { DataShape, Flow, Integration, StepKind } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  IChoiceStepRouteParams,
  IChoiceStepRouteState,
  IPageWithEditorBreadcrumb,
} from '../interfaces';
import { WithDescribeDataShapeForm } from '../shape/WithDescribeDataShapeForm';
import { toUIStep, toUIStepCollection } from '../utils';
import { createChoiceConfiguration } from "./utils";

export interface IDescribeChoiceDataShapePageProps extends IPageWithEditorBreadcrumb {
  backHref: (
    p: IChoiceStepRouteParams,
    s: IChoiceStepRouteState
  ) => H.LocationDescriptor;
  cancelHref: (
    p: IChoiceStepRouteParams,
    s: IChoiceStepRouteState
  ) => H.LocationDescriptor;
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  postConfigureHref: (
    integration: Integration,
    p: IChoiceStepRouteParams,
    s: IChoiceStepRouteState
  ) => H.LocationDescriptorObject;
}

export class DescribeChoiceDataShapePage extends React.Component<
  IDescribeChoiceDataShapePageProps
> {
  public render() {
    return (
      <>
        <WithIntegrationHelpers>
          {({ updateStep }) => (
            <WithRouteData<
              IChoiceStepRouteParams,
              IChoiceStepRouteState
            >>
              {(params, state, { history }) => {
                const positionAsNumber = parseInt(params.position, 10);
                const descriptor = state.step.action!.descriptor!;
                const dataShape: DataShape = descriptor.outputDataShape!;
                const configMode = getChoiceConfigMode(state.step);
                const backHref = this.props.backHref(
                  {
                    ...params,
                    configMode,
                  } as IChoiceStepRouteParams,
                  {
                    ...state,
                  } as IChoiceStepRouteState
                );
                const configuration = createChoiceConfiguration(
                  state.step.configuredProperties || {}
                );
                const handleUpdatedDataShape = async (
                  newDataShape: DataShape
                ) => {
                  const newDescriptor = { ...descriptor, outputDataShape: newDataShape };
                  const updatedAction = {
                    ...state.step.action!,
                    descriptor: newDescriptor,
                  };
                  const updatedStep = {
                    ...state.step,
                    action: updatedAction,
                  };
                  const updatedFlows = configuration.flows.map(f => {
                    return getFlow(
                      state.updatedIntegration || state.integration,
                      f.flow
                    ) || {} as Flow
                  }).filter(f => typeof f.name !== 'undefined');
                  if (typeof configuration.defaultFlow !== 'undefined') {
                    const defaultFlow = getFlow(
                      state.updatedIntegration || state.integration,
                      configuration.defaultFlow
                    );
                    if (defaultFlow) {
                      updatedFlows.push(defaultFlow!);
                    }
                  }
                  const updatedIntegration = await (updateStep)(
                    state.updatedIntegration || state.integration,
                    updatedStep,
                    params.flowId,
                    positionAsNumber,
                    state.step.configuredProperties
                  );
                  const stepWithUpdatedDescriptor = getStep(
                    updatedIntegration,
                    params.flowId,
                    positionAsNumber
                  ) as StepKind;
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
                        updatedIntegration: reconciledIntegration,
                      } as IChoiceStepRouteState
                    )
                  );
                };
                return (
                  <>
                    <PageTitle title={'Specify Conditional Flows Data Type'} />
                    <IntegrationEditorLayout
                      title='Specify Output Data Type'
                      description={
                        'Enter information that defines the data type of the step. All flows must produce this data type.'
                      }
                      toolbar={this.props.getBreadcrumb(
                        'Conditional Flows Data Type',
                        params,
                        state
                      )}
                      sidebar={this.props.sidebar({
                        activeIndex: positionAsNumber,
                        activeStep: {
                          ...toUIStep(state.step),
                        },
                        steps: toUIStepCollection(
                          getSteps(state.integration, params.flowId)
                        ),
                      })}
                      content={
                        <WithDescribeDataShapeForm
                          key={JSON.stringify(dataShape)}
                          initialKind={toDataShapeKinds(dataShape.kind)}
                          initialDefinition={dataShape.specification}
                          initialName={dataShape.name}
                          initialDescription={dataShape.description}
                          onUpdatedDataShape={handleUpdatedDataShape}
                          backActionHref={backHref}
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
      </>
    );
  }
}
