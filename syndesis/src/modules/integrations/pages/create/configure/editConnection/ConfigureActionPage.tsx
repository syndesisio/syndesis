import { WithIntegrationHelpers } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { Breadcrumb, ContentWithSidebarLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../../../../containers';
import { PageTitle } from '../../../../../../containers/PageTitle';
import {
  IntegrationEditorConfigureConnection,
  IntegrationEditorSidebar,
  IOnUpdatedIntegrationProps,
} from '../../../../components';
import resolvers from '../../../../resolvers';
import {
  getConfigureConnectionHrefCallback,
  getConfigureStepHrefCallback,
} from '../../../resolversHelpers';

export interface IConfigureActionRouteParams {
  position: string;
  actionId: string;
  step?: string;
}

export interface IConfigureActionRouteState {
  integration: Integration;
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
                { integration },
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
                  const updatedIntegration = await updateConnection(
                    integration,
                    stepObject.connection!,
                    action,
                    0,
                    positionAsNumber,
                    values
                  );
                  if (moreConfigurationSteps) {
                    history.push(
                      resolvers.create.configure.addConnection.configureAction({
                        actionId,
                        connection: stepObject.connection!,
                        integration: updatedIntegration,
                        position,
                        step: stepAsNumber + 1,
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
                  <>
                    <PageTitle title={'Configure the action'} />
                    <ContentWithSidebarLayout
                      sidebar={
                        <IntegrationEditorSidebar
                          steps={getSteps(integration, 0)}
                          configureConnectionHref={getConfigureConnectionHrefCallback(
                            integration
                          )}
                          configureStepHref={getConfigureStepHrefCallback(
                            integration
                          )}
                          activeIndex={positionAsNumber}
                        />
                      }
                      content={
                        <IntegrationEditorConfigureConnection
                          breadcrumb={
                            <Breadcrumb>
                              <Link to={resolvers.list()}>Integrations</Link>
                              <Link
                                to={resolvers.create.start.selectConnection()}
                              >
                                New integration
                              </Link>
                              <Link
                                to={resolvers.create.configure.index({
                                  integration,
                                })}
                              >
                                Save or add step
                              </Link>
                              <Link
                                to={resolvers.create.configure.editConnection.selectAction(
                                  {
                                    position,
                                    integration,
                                    connection: stepObject.connection!,
                                  }
                                )}
                              >
                                Choose action
                              </Link>
                              <span>Configure the action</span>
                            </Breadcrumb>
                          }
                          connection={stepObject.connection!}
                          actionId={actionId}
                          configurationStep={stepAsNumber}
                          backLink={resolvers.create.configure.editConnection.selectAction(
                            {
                              position,
                              integration,
                              connection: stepObject.connection!,
                            }
                          )}
                          initialValue={stepObject.configuredProperties}
                          onUpdatedIntegration={onUpdatedIntegration}
                        />
                      }
                    />
                  </>
                );
              }}
            </WithRouteData>
          )}
        </WithIntegrationHelpers>
      </WithClosedNavigation>
    );
  }
}
