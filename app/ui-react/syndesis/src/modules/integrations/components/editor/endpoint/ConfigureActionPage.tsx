import { getSteps, WithIntegrationHelpers } from '@syndesis/api';
import { Connection, Integration, Step } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as H from 'history';
import * as React from 'react';
import { PageTitle } from '../../../../../shared';
import {
  IOnUpdatedIntegrationProps,
  WithConfigurationForm,
} from '../../../shared';
import {
  IConfigureActionRouteParams,
  IConfigureActionRouteState,
} from '../interfaces';

export interface IConfigureActionPageProps {
  backHref: (
    p: IConfigureActionRouteParams,
    s: IConfigureActionRouteState
  ) => H.LocationDescriptor;
  cancelHref: (
    p: IConfigureActionRouteParams,
    s: IConfigureActionRouteState
  ) => H.LocationDescriptor;
  header: React.ReactNode;
  mode: 'adding' | 'editing';
  nextStepHref: (
    p: IConfigureActionRouteParams,
    s: IConfigureActionRouteState
  ) => H.LocationDescriptorObject;
  sidebar: (props: {
    connection: Connection;
    steps: Step[];
    activeIndex: number;
  }) => React.ReactNode;
  postConfigureHref: (
    integration: Integration,
    flow: string
  ) => H.LocationDescriptorObject;
}

/**
 * This page shows the configuration form for a given action.
 *
 * Submitting the form will update an *existing* integration step in
 * the [position specified in the params]{@link IConfigureActionRouteParams#position}
 * of the first flow, set up as specified by the form values.
 *
 * This component expects some [url params]{@link IConfigureActionRouteParams}
 * and [state]{@link IConfigureActionRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export class ConfigureActionPage extends React.Component<
  IConfigureActionPageProps
> {
  public render() {
    return (
      <WithIntegrationHelpers>
        {({ addConnection, updateConnection }) => (
          <WithRouteData<
            IConfigureActionRouteParams,
            IConfigureActionRouteState
          >>
            {(
              { actionId, flow, step = '0', position },
              {
                configuredProperties,
                connection,
                integration,
                updatedIntegration,
              },
              { history }
            ) => {
              const stepAsNumber = parseInt(step, 10);
              const flowAsNumber = parseInt(flow, 10);
              const positionAsNumber = parseInt(position, 10);
              const onUpdatedIntegration = async ({
                action,
                moreConfigurationSteps,
                values,
              }: IOnUpdatedIntegrationProps) => {
                updatedIntegration = await (this.props.mode === 'adding' &&
                  stepAsNumber === 0
                  ? addConnection
                  : updateConnection)(
                  updatedIntegration || integration,
                  connection,
                  action,
                  flowAsNumber,
                  positionAsNumber,
                  values
                );
                if (moreConfigurationSteps) {
                  history.push(
                    this.props.nextStepHref(
                      {
                        actionId,
                        flow,
                        position,
                        step: `${stepAsNumber + 1}`,
                      },
                      {
                        configuredProperties,
                        connection,
                        integration,
                        updatedIntegration,
                      }
                    )
                  );
                } else {
                  history.push(
                    this.props.postConfigureHref(updatedIntegration, flow)
                  );
                }
              };

              return (
                <WithConfigurationForm
                  connection={connection}
                  actionId={actionId}
                  configurationStep={stepAsNumber}
                  initialValue={configuredProperties}
                  onUpdatedIntegration={onUpdatedIntegration}
                >
                  {({ form, submitForm, isSubmitting }) => (
                    <>
                      <PageTitle title={'Configure the action'} />
                      <IntegrationEditorLayout
                        header={this.props.header}
                        sidebar={this.props.sidebar({
                          activeIndex: positionAsNumber,
                          connection,
                          steps: getSteps(updatedIntegration || integration, 0),
                        })}
                        content={form}
                        backHref={this.props.backHref(
                          { actionId, flow, step, position },
                          {
                            configuredProperties,
                            connection,
                            integration,
                            updatedIntegration,
                          }
                        )}
                        cancelHref={this.props.cancelHref(
                          { actionId, flow, step, position },
                          {
                            configuredProperties,
                            connection,
                            integration,
                            updatedIntegration,
                          }
                        )}
                        onNext={submitForm}
                        isNextLoading={isSubmitting}
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
