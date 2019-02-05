import { WithIntegrationHelpers } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import {
  IntegrationEditorLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../containers/PageTitle';
import { IntegrationCreatorBreadcrumbs } from '../../../components';
import {
  IOnUpdatedIntegrationProps,
  WithConfigurationForm,
} from '../../../containers';
import resolvers from '../../../resolvers';

/**
 * @param actionId - the ID of the action that should be configured
 * @param step - the configuration step when configuring a multi-page connection.
 */
export interface IFinishConfigurationPageRouteParams {
  actionId: string;
  step?: string;
}

/**
 * @param startConnection - the connection object selected in step 1.1. Needed
 * to render the IVP.
 * @param startAction - the action object selected in step 1.2. Needed to
 * render the IVP.
 * @oaram integration - the integration object created in step 1.3.
 * @param finishConnection - the connection object selected in step 2.1. Needed
 * to render the IVP.
 * @param updatedIntegration - when creating a link to this page, this should
 * never be set. It is used by the page itself to pass the partially configured
 * step when configuring a multi-page connection.
 */
export interface IFinishConfigurationPageRouteState {
  startAction: Action;
  startConnection: ConnectionOverview;
  finishConnection: ConnectionOverview;
  integration: Integration;
  updatedIntegration: Integration;
}

/**
 * This page shows the configuration form for a given action. It's supposed to
 * be used for step 2.3 of the creation wizard.
 *
 * Submitting the form will update the integration object with the second step
 * of the first flow set up as specified by the form values.
 *
 * This component expects some [url params]{@link IFinishConfigurationPageRouteParams}
 * and [state]{@link IFinishConfigurationPageRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 *
 * @todo DRY the connection icon code
 */
export class FinishConfigurationPage extends React.Component {
  public render() {
    return (
      <WithIntegrationHelpers>
        {({ addConnection, getStep, updateOrAddConnection }) => (
          <WithRouteData<
            IFinishConfigurationPageRouteParams,
            IFinishConfigurationPageRouteState
          >>
            {(
              { actionId, step = '0' },
              {
                startAction,
                startConnection,
                finishConnection,
                integration,
                updatedIntegration,
              },
              { history }
            ) => {
              const stepAsNumber = parseInt(step, 10);
              const position = integration.flows![0].steps!.length;
              let initialValue;
              try {
                const stepObject = getStep(
                  updatedIntegration || integration,
                  0,
                  0
                );
                initialValue = stepObject.configuredProperties;
              } catch (e) {
                // noop
              }
              const onUpdatedIntegration = async ({
                action,
                moreConfigurationSteps,
                values,
              }: IOnUpdatedIntegrationProps) => {
                updatedIntegration = await updateOrAddConnection(
                  updatedIntegration || integration,
                  finishConnection,
                  action,
                  0,
                  position,
                  values
                );
                if (moreConfigurationSteps) {
                  history.push(
                    resolvers.create.finish.configureAction({
                      actionId,
                      finishConnection,
                      integration,
                      startAction,
                      startConnection,
                      step: stepAsNumber + 1,
                      updatedIntegration,
                    })
                  );
                } else {
                  history.push(
                    resolvers.create.configure.index({
                      integration: updatedIntegration,
                    })
                  );
                }
              };
              return (
                <WithConfigurationForm
                  connection={finishConnection}
                  actionId={actionId}
                  configurationStep={stepAsNumber}
                  initialValue={initialValue}
                  onUpdatedIntegration={onUpdatedIntegration}
                >
                  {({ form, submitForm, isSubmitting }) => (
                    <>
                      <PageTitle title={'Configure the action'} />
                      <IntegrationEditorLayout
                        header={
                          <IntegrationCreatorBreadcrumbs step={2} subStep={2} />
                        }
                        sidebar={
                          <IntegrationVerticalFlow>
                            {({ expanded }) => (
                              <>
                                <IntegrationFlowStepWithOverview
                                  icon={
                                    <img
                                      src={startConnection.icon}
                                      width={24}
                                      height={24}
                                    />
                                  }
                                  i18nTitle={`1. ${startAction.name}`}
                                  i18nTooltip={`1. ${startAction.name}`}
                                  active={false}
                                  showDetails={expanded}
                                  name={startConnection.connector!.name}
                                  action={startAction.name}
                                  dataType={'TODO'}
                                />
                                <IntegrationFlowStepGeneric
                                  icon={
                                    <img
                                      src={finishConnection.icon}
                                      width={24}
                                      height={24}
                                    />
                                  }
                                  i18nTitle={`${
                                    finishConnection.connector!.name
                                  }`}
                                  i18nTooltip={`2. ${finishConnection.name}`}
                                  active={true}
                                  showDetails={expanded}
                                  description={'Configure the action'}
                                />
                              </>
                            )}
                          </IntegrationVerticalFlow>
                        }
                        content={form}
                        backHref={resolvers.create.finish.selectAction({
                          finishConnection,
                          integration,
                          startAction,
                          startConnection,
                        })}
                        cancelHref={resolvers.list()}
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
