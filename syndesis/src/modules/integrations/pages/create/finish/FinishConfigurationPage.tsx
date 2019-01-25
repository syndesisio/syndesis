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
import { WithClosedNavigation } from '../../../../../containers';
import { PageTitle } from '../../../../../containers/PageTitle';
import { IntegrationEditorBreadcrumbs } from '../../../components';
import {
  IOnUpdatedIntegrationProps,
  WithConfigurationForm,
} from '../../../containers';
import resolvers from '../../../resolvers';

export interface IFinishConfigurationPageRouteParams {
  actionId: string;
  connectionId: string;
  step?: string;
}

export interface IFinishConfigurationPageRouteState {
  startAction: Action;
  startConnection: ConnectionOverview;
  finishConnection: ConnectionOverview;
  integration: Integration;
  updatedIntegration: Integration;
}

export class FinishConfigurationPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithIntegrationHelpers>
          {({ addConnection, getStep, updateOrAddConnection }) => (
            <WithRouteData<
              IFinishConfigurationPageRouteParams,
              IFinishConfigurationPageRouteState
            >>
              {(
                { actionId, connectionId, step = '0' },
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
                    {({ form, onSubmit, isSubmitting }) => (
                      <>
                        <PageTitle title={'Configure the action'} />
                        <IntegrationEditorLayout
                          header={
                            <IntegrationEditorBreadcrumbs
                              step={2}
                              subStep={2}
                            />
                          }
                          sidebar={
                            <IntegrationVerticalFlow disabled={true}>
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
                          onNext={onSubmit}
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
      </WithClosedNavigation>
    );
  }
}
