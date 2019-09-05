import {
  getStep,
  getSteps,
  isEndStep,
  isMiddleStep,
  isStartStep,
  requiresOutputDescribeDataShape,
  toDataShapeKinds,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { DataShape, Integration, StepKind } from '@syndesis/models';
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
import { WithDescribeDataShapeForm } from '../shape/WithDescribeDataShapeForm';
import { toUIStep, toUIStepCollection } from '../utils';

export interface IDescribeDataShapePageProps extends IPageWithEditorBreadcrumb {
  backHref: (
    page: 'describeData' | 'configureAction',
    p: IConfigureActionRouteParams | IDescribeDataShapeRouteParams,
    s: IConfigureActionRouteState | IDescribeDataShapeRouteState
  ) => H.LocationDescriptor;
  cancelHref: (
    p: IDescribeDataShapeRouteParams,
    s: IDescribeDataShapeRouteState
  ) => H.LocationDescriptor;
  mode: 'adding' | 'editing';
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  postConfigureHref: (
    page: 'describeData' | 'addStep',
    integration: Integration,
    p: IDescribeDataShapeRouteParams | IConfigureActionRouteParams,
    s: IDescribeDataShapeRouteState | IConfigureActionRouteState
  ) => H.LocationDescriptorObject;
}

export class DescribeDataShapePage extends React.Component<
  IDescribeDataShapePageProps
> {
  public render() {
    return (
      <>
        <WithIntegrationHelpers>
          {({ addConnection, updateConnection }) => (
            <WithRouteData<
              IDescribeDataShapeRouteParams,
              IDescribeDataShapeRouteState
            >>
              {(params, state, { history }) => {
                const positionAsNumber = parseInt(params.position, 10);
                const title =
                  params.direction === DataShapeDirection.INPUT
                    ? 'Specify Input Data Type'
                    : 'Specify Output Data Type';
                const descriptor = state.step.action!.descriptor!;
                const dataShape: DataShape =
                  params.direction === DataShapeDirection.INPUT
                    ? descriptor.inputDataShape!
                    : descriptor.outputDataShape!;
                const backDescribeData = this.props.backHref(
                  'describeData',
                  {
                    ...params,
                    direction: DataShapeDirection.INPUT,
                  },
                  state
                );
                const backActionConfig = this.props.backHref(
                  'configureAction',
                  {
                    ...params,
                    actionId: state.step.action!.id!,
                    page: '0',
                  },
                  state
                );
                const backHref =
                  isMiddleStep(
                    state.integration,
                    params.flowId,
                    positionAsNumber
                  ) && params.direction === DataShapeDirection.OUTPUT
                    ? backDescribeData
                    : backActionConfig;
                const handleUpdatedDataShape = async (
                  newDataShape: DataShape
                ) => {
                  const newDescriptor =
                    params.direction === DataShapeDirection.INPUT
                      ? { ...descriptor, inputDataShape: newDataShape }
                      : { ...descriptor, outputDataShape: newDataShape };
                  const action = {
                    ...state.step.action!,
                    descriptor: newDescriptor,
                  };
                  const updatedIntegration = await (this.props.mode === 'adding'
                    ? addConnection
                    : updateConnection)(
                    state.updatedIntegration || state.integration,
                    state.connection,
                    action,
                    params.flowId,
                    positionAsNumber,
                    state.step.configuredProperties
                  );
                  const stepKind = getStep(
                    updatedIntegration,
                    params.flowId,
                    positionAsNumber
                  ) as StepKind;
                  const gotoDescribeData = (
                    nextDirection: DataShapeDirection
                  ) => {
                    history.push(
                      this.props.postConfigureHref(
                        'describeData',
                        updatedIntegration!,
                        {
                          ...params,
                          direction: nextDirection,
                        },
                        {
                          ...state,
                          step: stepKind,
                          updatedIntegration,
                        }
                      )
                    );
                  };
                  const gotoDefaultNextPage = () => {
                    history.push(
                      this.props.postConfigureHref(
                        'addStep',
                        updatedIntegration!,
                        {
                          ...params,
                          actionId: stepKind.action!.id!,
                          page: '0',
                        } as IConfigureActionRouteParams,
                        {
                          ...state,
                          configuredProperties: stepKind.configuredProperties,
                          step: '0',
                          updatedIntegration,
                        } as IConfigureActionRouteState
                      )
                    );
                  };
                  if (
                    isStartStep(
                      updatedIntegration,
                      params.flowId,
                      positionAsNumber
                    )
                  ) {
                    gotoDefaultNextPage();
                  } else if (
                    isEndStep(
                      updatedIntegration,
                      params.flowId,
                      positionAsNumber
                    )
                  ) {
                    gotoDefaultNextPage();
                  } else {
                    if (
                      params.direction === DataShapeDirection.INPUT &&
                      requiresOutputDescribeDataShape(descriptor)
                    ) {
                      gotoDescribeData(DataShapeDirection.OUTPUT);
                    } else {
                      gotoDefaultNextPage();
                    }
                  }
                };
                return (
                  <>
                    <PageTitle title={'Specify Data Type'} />
                    <IntegrationEditorLayout
                      title={title}
                      description={
                        'This is a typeless connection. To use type-aware functionality, enter information that defines the data type'
                      }
                      toolbar={this.props.getBreadcrumb(
                        'Specify data type',
                        params,
                        state
                      )}
                      sidebar={this.props.sidebar({
                        activeIndex: positionAsNumber,
                        activeStep: {
                          ...toUIStep(state.step.connection!),
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
