import { getSteps, WithIntegrationHelpers } from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  IConfigureStepRouteParams,
  IConfigureStepRouteState,
  IPageWithEditorBreadcrumb,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import {
  IOnUpdatedIntegrationProps,
  WithConfigurationForm,
} from './WithConfigurationForm';

export interface IConfigureStepPageProps extends IPageWithEditorBreadcrumb {
  cancelHref: (
    p: IConfigureStepRouteParams,
    s: IConfigureStepRouteState
  ) => H.LocationDescriptor;
  mode: 'adding' | 'editing';
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
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
            {(params, state, { history }) => {
              const positionAsNumber = parseInt(params.position, 10);
              const onUpdatedIntegration = async ({
                values,
              }: IOnUpdatedIntegrationProps) => {
                const updatedIntegration = await (this.props.mode === 'adding'
                  ? addStep
                  : updateStep)(
                  state.updatedIntegration || state.integration,
                  state.step,
                  params.flowId,
                  positionAsNumber,
                  values
                );

                history.push(
                  this.props.postConfigureHref(updatedIntegration, params, {
                    ...state,
                    updatedIntegration,
                  })
                );
              };

              return (
                <WithConfigurationForm
                  step={state.step}
                  onUpdatedIntegration={onUpdatedIntegration}
                >
                  {({ form }) => (
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
                          activeStep: toUIStep(state.step),
                          steps: toUIStepCollection(
                            getSteps(
                              state.updatedIntegration || state.integration,
                              params.flowId
                            )
                          ),
                        })}
                        content={form}
                        cancelHref={this.props.cancelHref(params, state)}
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
