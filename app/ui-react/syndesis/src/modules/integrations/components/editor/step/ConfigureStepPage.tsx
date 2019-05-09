import { getSteps, WithIntegrationHelpers } from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration, StepKind } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../shared';
import {
  IConfigureStepRouteParams,
  IConfigureStepRouteState,
  IUIStep,
} from '../interfaces';
import { toUIStepKindCollection } from '../utils';
import {
  IOnUpdatedIntegrationProps,
  WithConfigurationForm,
} from './WithConfigurationForm';

export interface IConfigureStepPageProps {
  backHref: (
    p: IConfigureStepRouteParams,
    s: IConfigureStepRouteState
  ) => H.LocationDescriptor;
  cancelHref: (
    p: IConfigureStepRouteParams,
    s: IConfigureStepRouteState
  ) => H.LocationDescriptor;
  mode: 'adding' | 'editing';
  sidebar: (props: {
    step: StepKind;
    steps: IUIStep[];
    activeIndex: number;
  }) => React.ReactNode;
  postConfigureHref: (
    integration: Integration,
    p: IConfigureStepRouteParams,
    s: IConfigureStepRouteState
  ) => H.LocationDescriptorObject;
}

/**
 * This page shows the configuration form for a given action.
 *
 * Submitting the form will update an *existing* integration step in
 * the [position specified in the params]{@link IConfigureStepRouteParams#position}
 * of the first flow, set up as specified by the form values.
 *
 * This component expects some [url params]{@link IConfigureStepRouteParams}
 * and [state]{@link IConfigureStepRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export class ConfigureStepPage extends React.Component<
  IConfigureStepPageProps
> {
  public render() {
    return (
      <WithIntegrationHelpers>
        {({ addStep, updateStep }) => (
          <WithRouteData<IConfigureStepRouteParams, IConfigureStepRouteState>>
            {(
              { flowId, position },
              { step, integration, updatedIntegration },
              { history }
            ) => {
              const positionAsNumber = parseInt(position, 10);
              const onUpdatedIntegration = async ({
                values,
              }: IOnUpdatedIntegrationProps) => {
                updatedIntegration = await (this.props.mode === 'adding'
                  ? addStep
                  : updateStep)(
                  updatedIntegration || integration,
                  step,
                  flowId,
                  positionAsNumber,
                  values
                );

                history.push(
                  this.props.postConfigureHref(
                    updatedIntegration,
                    { flowId, position },
                    {
                      integration,
                      step,
                      updatedIntegration,
                    }
                  )
                );
              };

              return (
                <WithConfigurationForm
                  step={step}
                  onUpdatedIntegration={onUpdatedIntegration}
                  chooseActionHref={this.props.backHref(
                    { flowId, position },
                    {
                      integration,
                      step,
                      updatedIntegration,
                    }
                  )}
                >
                  {({ form }) => (
                    <>
                      <PageTitle title={'Configure the action'} />
                      <IntegrationEditorLayout
                        title={'Configure the action'}
                        description={
                          'Fill in the required information for the selected action.'
                        }
                        sidebar={this.props.sidebar({
                          activeIndex: positionAsNumber,
                          step,
                          steps: toUIStepKindCollection(
                            getSteps(updatedIntegration || integration, flowId)
                          ),
                        })}
                        content={form}
                        cancelHref={this.props.cancelHref(
                          { flowId, position },
                          {
                            integration,
                            step,
                            updatedIntegration,
                          }
                        )}
                      />
                    </>
                  )}
                </WithConfigurationForm>
              );
            }}
          </WithRouteData>
        )}
      </WithIntegrationHelpers>
    );
  }
}
