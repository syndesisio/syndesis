import { getSteps, WithIntegrationHelpers } from '@syndesis/api';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../../shared';
import {
  IntegrationCreatorBreadcrumbs,
  IntegrationEditorSidebar,
} from '../../../../components';
import resolvers from '../../../../resolvers';
import {
  IOnUpdatedIntegrationProps,
  WithConfigurationForm,
} from '../../../../shared';
import {
  IConfigureActionRouteParams,
  IConfigureActionRouteState,
} from '../../../editorInterfaces';

/**
 * This page shows the configuration form for a given action. It's supposed to
 * be used for step 3.edit.3 of the creation wizard.
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
export class ConfigureActionPage extends React.Component {
  public render() {
    return (
      <WithIntegrationHelpers>
        {({ updateConnection }) => (
          <WithRouteData<
            IConfigureActionRouteParams,
            IConfigureActionRouteState
          >>
            {(
              { actionId, step = '0', position },
              {
                configuredProperties,
                connection,
                integration,
                updatedIntegration,
              },
              { history }
            ) => {
              const stepAsNumber = parseInt(step, 10);
              const positionAsNumber = parseInt(position, 10);
              const onUpdatedIntegration = async ({
                action,
                moreConfigurationSteps,
                values,
              }: IOnUpdatedIntegrationProps) => {
                updatedIntegration = await updateConnection(
                  updatedIntegration || integration,
                  connection,
                  action,
                  0,
                  positionAsNumber,
                  values
                );
                if (moreConfigurationSteps) {
                  history.push(
                    resolvers.create.configure.addStep.configureAction({
                      actionId,
                      connection,
                      flow: '0',
                      integration,
                      position,
                      step: stepAsNumber + 1,
                      updatedIntegration,
                    })
                  );
                } else {
                  history.push(
                    resolvers.create.configure.index({
                      flow: '0',
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
                  initialValue={configuredProperties}
                  onUpdatedIntegration={onUpdatedIntegration}
                >
                  {({ form, submitForm, isSubmitting }) => (
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
                            activeIndex={positionAsNumber}
                          />
                        }
                        content={form}
                        backHref={resolvers.create.configure.editStep.selectAction(
                          {
                            connection,
                            flow: '0',
                            integration,
                            position,
                          }
                        )}
                        cancelHref={resolvers.create.configure.index({
                          flow: '0',
                          integration,
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
