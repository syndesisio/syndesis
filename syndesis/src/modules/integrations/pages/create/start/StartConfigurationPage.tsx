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

export interface IStartConfigurationPageRouteParams {
  actionId: string;
  connectionId: string;
  step?: string;
}

export interface IStartConfigurationPageRouteState {
  connection: ConnectionOverview;
  integration?: Integration;
  updatedIntegration?: Integration;
}

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
              { actionId, connectionId, step = '0' },
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
                  {({ form, onSubmit, isSubmitting }) => (
                    <>
                      <PageTitle title={'Configure the action'} />
                      <IntegrationEditorLayout
                        header={
                          <IntegrationCreatorBreadcrumbs step={1} subStep={2} />
                        }
                        sidebar={
                          <IntegrationVerticalFlow disabled={true}>
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
    );
  }
}
