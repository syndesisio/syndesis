import { WithIntegrationHelpers } from '@syndesis/api';
import { ConnectionOverview, Integration } from '@syndesis/models';
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
export interface IStartConfigurationPageRouteParams {
  actionId: string;
  step?: string;
}

/**
 * @param connection - the connection object selected in step 1.1
 * @param integration - an optional integration. It's expected to be undefined
 * when reaching this step for the first time, but it should have a valid
 * integration object if this step is reached from a back button, in order to
 * show the previously configured values to the user.
 * @param updatedIntegration - when creating a link to this page, this should
 * never be set. It is used by the page itself to pass the partially configured
 * step when configuring a multi-page connection.
 */
export interface IStartConfigurationPageRouteState {
  connection: ConnectionOverview;
  integration?: Integration;
  updatedIntegration?: Integration;
}

/**
 * This page shows the configuration form for a given action. It's supposed to
 * be used for step 1.3 of the creation wizard.
 *
 * Submitting the form will create a new integration object with the first step
 * of the first flow set up as specified by the form values.
 *
 * This component expects some [url params]{@link IStartConfigurationPageRouteParams}
 * and [state]{@link IStartConfigurationPageRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 *
 * @todo DRY the connection icon code
 */
export class StartConfigurationPage extends React.Component {
  public render() {
    return (
      <WithIntegrationHelpers>
        {({ getEmptyIntegration, getStep, updateOrAddConnection }) => (
          <WithRouteData<
            IStartConfigurationPageRouteParams,
            IStartConfigurationPageRouteState
          >>
            {(
              { actionId, step = '0' },
              {
                connection,
                integration = getEmptyIntegration(),
                updatedIntegration,
              },
              { history }
            ) => {
              const stepAsNumber = parseInt(step, 10);
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
                  connection,
                  action,
                  0,
                  0,
                  values
                );
                if (moreConfigurationSteps) {
                  history.push(
                    resolvers.create.start.configureAction({
                      actionId,
                      connection,
                      integration,
                      step: stepAsNumber + 1,
                      updatedIntegration,
                    })
                  );
                } else {
                  history.push(
                    resolvers.create.finish.selectConnection({
                      integration: updatedIntegration,
                      startAction: action,
                      startConnection: connection,
                    })
                  );
                }
              };
              return (
                <WithConfigurationForm
                  connection={connection}
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
                          <IntegrationCreatorBreadcrumbs step={1} subStep={2} />
                        }
                        sidebar={
                          <IntegrationVerticalFlow>
                            {({ expanded }) => (
                              <>
                                <IntegrationFlowStepGeneric
                                  icon={
                                    <img
                                      src={connection.icon}
                                      width={24}
                                      height={24}
                                    />
                                  }
                                  i18nTitle={`1. ${connection.connector!.name}`}
                                  i18nTooltip={`1. ${connection.name}`}
                                  active={true}
                                  showDetails={expanded}
                                  description={'Configure the action'}
                                />
                                <IntegrationFlowStepWithOverview
                                  icon={<i className={'fa fa-plus'} />}
                                  i18nTitle={'2. Finish'}
                                  i18nTooltip={'Finish'}
                                  active={false}
                                  showDetails={expanded}
                                  name={'n/a'}
                                  action={'n/a'}
                                  dataType={'n/a'}
                                />
                              </>
                            )}
                          </IntegrationVerticalFlow>
                        }
                        content={form}
                        cancelHref={resolvers.list()}
                        backHref={resolvers.create.start.selectAction({
                          connection,
                        })}
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
