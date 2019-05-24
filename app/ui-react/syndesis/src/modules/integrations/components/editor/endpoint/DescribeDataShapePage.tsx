import {
  getConnectionIcon,
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
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import { WithDescribeDataShapeForm } from './WithDescribeDataShapeForm';

export interface IDescribeDataShapePageProps {
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
              {(
                { direction, flowId, position },
                { connection, step, integration, updatedIntegration },
                { history }
              ) => {
                const positionAsNumber = parseInt(position, 10);
                const title =
                  direction === DataShapeDirection.INPUT
                    ? 'Specify Input Data Type'
                    : 'Specify Output Data Type';
                const descriptor = step.action!.descriptor!;
                const dataShape: DataShape =
                  direction === DataShapeDirection.INPUT
                    ? descriptor.inputDataShape!
                    : descriptor.outputDataShape!;
                const backDescribeData = this.props.backHref(
                  'describeData',
                  {
                    direction: DataShapeDirection.INPUT,
                    flowId,
                    position,
                  } as IDescribeDataShapeRouteParams,
                  {
                    connection,
                    integration,
                    step,
                    updatedIntegration,
                  } as IDescribeDataShapeRouteState
                );
                const backActionConfig = this.props.backHref(
                  'configureAction',
                  {
                    actionId: step.action!.id!,
                    flowId,
                    position,
                    step: '0',
                  } as IConfigureActionRouteParams,
                  {
                    connection,
                    integration,
                    updatedIntegration,
                  } as IConfigureActionRouteState
                );
                const backHref =
                  isMiddleStep(integration, flowId, positionAsNumber) &&
                  direction === DataShapeDirection.OUTPUT
                    ? backDescribeData
                    : backActionConfig;
                const handleUpdatedDataShape = async (
                  newDataShape: DataShape
                ) => {
                  const newDescriptor =
                    direction === DataShapeDirection.INPUT
                      ? { ...descriptor, inputDataShape: newDataShape }
                      : { ...descriptor, outputDataShape: newDataShape };
                  const action = { ...step.action!, descriptor: newDescriptor };
                  updatedIntegration = await (this.props.mode === 'adding'
                    ? addConnection
                    : updateConnection)(
                    updatedIntegration || integration,
                    connection,
                    action,
                    flowId,
                    positionAsNumber,
                    step.configuredProperties
                  );
                  const stepKind = getStep(
                    updatedIntegration,
                    flowId,
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
                          direction: nextDirection,
                          flowId,
                          position,
                        },
                        {
                          connection,
                          integration,
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
                          actionId: stepKind.action!.id!,
                          flowId,
                          position,
                          step: '0',
                        } as IConfigureActionRouteParams,
                        {
                          configuredProperties: stepKind.configuredProperties,
                          connection,
                          integration,
                          step: '0',
                          updatedIntegration,
                        } as IConfigureActionRouteState
                      )
                    );
                  };
                  if (
                    isStartStep(updatedIntegration, flowId, positionAsNumber)
                  ) {
                    gotoDefaultNextPage();
                  } else if (
                    isEndStep(updatedIntegration, flowId, positionAsNumber)
                  ) {
                    gotoDefaultNextPage();
                  } else {
                    if (
                      direction === DataShapeDirection.INPUT &&
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
                      sidebar={this.props.sidebar({
                        activeIndex: positionAsNumber,
                        activeStep: {
                          ...toUIStep(step.connection!),
                          icon: getConnectionIcon(
                            process.env.PUBLIC_URL,
                            step.connection!
                          ),
                        },
                        steps: toUIStepCollection(
                          getSteps(integration, flowId)
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
                      cancelHref={this.props.cancelHref(
                        { flowId, direction, position },
                        {
                          connection,
                          integration,
                          step,
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
      </>
    );
  }
}
