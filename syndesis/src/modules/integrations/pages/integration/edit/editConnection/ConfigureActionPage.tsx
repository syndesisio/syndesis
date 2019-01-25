import { WithIntegrationHelpers } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { WithClosedNavigation } from '../../../../../../containers';
import { PageTitle } from '../../../../../../containers/PageTitle';
import {
  IntegrationEditorBreadcrumbs,
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
  integration: Integration;
  updatedIntegration?: Integration;
}

export class ConfigureActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithIntegrationHelpers>
          {({ getSteps, getStep, updateConnection }) => (
            <WithRouteData<
              IConfigureActionRouteParams,
              IConfigureActionRouteState
            >>
              {(
                { actionId, step = '0', position },
                { integration, updatedIntegration },
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
                  updatedIntegration = await updateConnection(
                    updatedIntegration || integration,
                    stepObject.connection!,
                    action,
                    0,
                    positionAsNumber,
                    values
                  );
                  if (moreConfigurationSteps) {
                    history.push(
                      resolvers.integration.edit.addConnection.configureAction({
                        actionId,
                        connection: stepObject.connection!,
                        integration,
                        position,
                        step: stepAsNumber + 1,
                        updatedIntegration,
                      })
                    );
                  } else {
                    history.push(
                      resolvers.integration.edit.index({
                        integration: updatedIntegration,
                      })
                    );
                  }
                };

                return (
                  <WithConfigurationForm
                    connection={stepObject.connection!}
                    actionId={actionId}
                    configurationStep={stepAsNumber}
                    initialValue={stepObject.configuredProperties}
                    onUpdatedIntegration={onUpdatedIntegration}
                  >
                    {({ form, onSubmit, isSubmitting }) => (
                      <>
                        <PageTitle title={'Configure the action'} />
                        <IntegrationEditorLayout
                          header={<IntegrationEditorBreadcrumbs step={1} />}
                          sidebar={
                            <IntegrationEditorSidebar
                              steps={getSteps(
                                updatedIntegration || integration,
                                0
                              )}
                              activeIndex={positionAsNumber}
                            />
                          }
                          content={form}
                          backHref={resolvers.integration.edit.editConnection.selectAction(
                            {
                              connection: stepObject.connection!,
                              integration,
                              position,
                            }
                          )}
                          cancelHref={resolvers.integration.edit.index({
                            integration,
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
      </WithClosedNavigation>
    );
  }
}
