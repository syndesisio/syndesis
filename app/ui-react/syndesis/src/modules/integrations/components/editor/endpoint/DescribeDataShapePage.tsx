import {
  getConnectionIcon,
  getSteps,
  toDataShapeKinds,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { DataShape } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  DataShapeDirection,
  IDescribeDataShapeRouteParams,
  IDescribeDataShapeRouteState,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import { WithDescribeDataShapeForm } from './WithDescribeDataShapeForm';

export interface IDescribeDataShapePageProps {
  /*
  backHref: (
    p: IDescribeDataShapeRouteParams,
    s: IDescribeDataShapeRouteState
  ) => H.LocationDescriptor;
  */
  cancelHref: (
    p: IDescribeDataShapeRouteParams,
    s: IDescribeDataShapeRouteState
  ) => H.LocationDescriptor;
  // mode: 'adding' | 'editing';
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
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
                const handleUpdatedDataShape = (newDataShape: DataShape) => {
                  /* todo */
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
                          initialKind={toDataShapeKinds(dataShape.kind)}
                          initialDefinition={dataShape.specification}
                          initialName={dataShape.name}
                          initialDescription={dataShape.description}
                          onUpdatedDataShape={handleUpdatedDataShape}
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
