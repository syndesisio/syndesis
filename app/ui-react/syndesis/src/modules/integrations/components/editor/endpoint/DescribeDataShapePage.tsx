import {
  getConnectionIcon,
  getSteps,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
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
                { step, integration, updatedIntegration },
                { history }
              ) => {
                const positionAsNumber = parseInt(position, 10);
                const title =
                  direction === DataShapeDirection.INPUT
                    ? 'Specify Input Data Type'
                    : 'Specify Output Data Type';
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
                      content={<p>TODO</p>}
                      cancelHref={this.props.cancelHref(
                        { flowId, direction, position },
                        {
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
