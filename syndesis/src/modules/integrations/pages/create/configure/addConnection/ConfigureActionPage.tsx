import { WithIntegrationHelpers } from '@syndesis/api';
import { ConnectionOverview, Integration } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { WithClosedNavigation } from '../../../../../../containers';
import { PageTitle } from '../../../../../../containers/PageTitle';
import {
  IntegrationCreatorBreadcrumbs,
  IntegrationEditorSidebar,
} from '../../../../components';
import {
  IOnUpdatedIntegrationProps,
  WithConfigurationForm,
} from '../../../../containers';
import resolvers from '../../../../resolvers';

export interface IConfigureActionRouteParams {
  position: string;
  actionId: string;
  step?: string;
}

export interface IConfigureActionRouteState {
  connection: ConnectionOverview;
  integration: Integration;
  updatedIntegration?: Integration;
}

export class ConfigureActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithIntegrationHelpers>
          {({ addConnection, getStep, getSteps, updateConnection }) => (
            <WithRouteData<
              IConfigureActionRouteParams,
              IConfigureActionRouteState
            >>
              {(
                { actionId, step = '0', position },
                { connection, integration, updatedIntegration },
                { history }
              ) => {
                const stepAsNumber = parseInt(step, 10);
                const positionAsNumber = parseInt(position, 10);
                const stepObject = getStep(integration, 0, positionAsNumber);
                const onUpdatedIntegration = async ({
                  action,
                  moreConfigurationSteps,
                  values,
                }: IOnUpdatedIntegrationProps) => {
                  updatedIntegration = await (stepAsNumber === 0
                    ? addConnection
                    : updateConnection)(
                    updatedIntegration || integration,
                    connection,
                    action,
                    0,
                    positionAsNumber,
                    values
                  );
                  if (moreConfigurationSteps) {
                    history.push(
                      resolvers.create.configure.addConnection.configureAction({
                        actionId,
                        connection,
                        integration,
                        position,
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
                    connection={connection}
                    actionId={actionId}
                    configurationStep={stepAsNumber}
                    initialValue={stepObject.configuredProperties}
                    onUpdatedIntegration={onUpdatedIntegration}
                  >
                    {({ form, onSubmit, isSubmitting }) => (
                      <>
                        <PageTitle title={'Configure the action'} />
                        <IntegrationEditorLayout
                          header={<IntegrationCreatorBreadcrumbs step={3} />}
                          sidebar={
                            <IntegrationEditorSidebar
                              steps={getSteps(
                                updatedIntegration || integration,
                                0
                              )}
                              addAtIndex={
                                stepAsNumber === 0
                                  ? positionAsNumber
                                  : undefined
                              }
                              addIcon={
                                <img
                                  src={connection.icon}
                                  width={24}
                                  height={24}
                                />
                              }
                              addI18nTitle={`${positionAsNumber + 1}. ${
                                connection.connector!.name
                              }`}
                              addI18nTooltip={`${positionAsNumber + 1}. ${
                                connection.name
                              }`}
                              addI18nDescription={'Configure the action'}
                            />
                          }
                          content={form}
                          cancelHref={resolvers.create.configure.index({
                            integration,
                          })}
                          backHref={resolvers.create.configure.addConnection.selectAction(
                            { position, integration, connection }
                          )}
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
