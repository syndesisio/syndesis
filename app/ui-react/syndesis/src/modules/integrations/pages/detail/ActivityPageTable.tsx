import { WithActivities } from '@syndesis/api';
import {
  Activity,
  ActivityStep,
  IntegrationDeployment,
  Step,
} from '@syndesis/models';
import {
  IntegrationDetailActivityItem,
  IntegrationDetailActivityItemSteps,
} from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../../i18n';

export interface IActivityPagetableProps {
  integrationId: string;
}

interface IExtendedActivity extends Activity {
  [name: string]: any;
}

interface IExtendedActivityStep extends ActivityStep {
  [name: string]: any;
}

interface IExtendedDeployment extends IntegrationDeployment {
  [name: string]: any;
}

function fetchStepName(step: Step): string {
  let stepName = 'n/a';

  if (step) {
    const { name, action } = step;
    stepName = name || (action && action.name ? action.name : stepName);
  }

  return stepName;
}

export class ActivityPageTable extends React.Component<
  IActivityPagetableProps
> {
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <WithActivities integrationId={this.props.integrationId}>
            {({
              activities: activitiesBase,
              deployments: deploymentsBase,
              // fetchActivities,
              // fetchDeployments,
            }) => {
              const activities = activitiesBase as IExtendedActivity[];
              const integrationDeployments = (deploymentsBase ||
                []) as IExtendedDeployment[];
              /*
              const refresh = async () => {
                await fetchActivities();
                await fetchDeployments();
              };
              */

              activities.forEach((activity: IExtendedActivity) => {
                if (activity.steps && Array.isArray(activity.steps)) {
                  activity.steps.forEach((step: IExtendedActivityStep) => {
                    step.name = 'n/a';
                    step.isFailed =
                      typeof step.failure !== 'undefined' &&
                      step.failure.length > 0;

                    const deployedIntegration = integrationDeployments.find(
                      deployment => deployment.version === +activity.ver
                    );
                    if (!deployedIntegration) {
                      return;
                    }

                    for (const integrationFlow of deployedIntegration!.spec!
                      .flows!) {
                      const integrationStep = integrationFlow!.steps!.find(
                        is => is.id === step.id
                      );
                      if (integrationStep) {
                        step.name = fetchStepName(integrationStep);
                        break;
                      }
                    }

                    const errorMessages = [
                      null,
                      ...step.messages!,
                      step.failure,
                    ].filter(messages => !!messages);
                    step.output =
                      errorMessages.length > 0 ? errorMessages.join('\n') : '';
                  });
                }
              });

              return (
                <>
                  {activities.map(
                    (activity: IExtendedActivity, activityIndex: number) => (
                      <IntegrationDetailActivityItem
                        key={activityIndex}
                        date={new Date(activity.at!).getDate().toLocaleString()}
                        errorCount={activity.failed ? 1 : 0}
                        i18nErrorsFound={t('integrations:ErrorsFound')}
                        i18nNoErrors={t('integrations:NoErrors')}
                        i18nNoSteps={t('integrations:NoSteps')}
                        i18nVersion={t('shared:Version')}
                        steps={(activity.steps || []).map(
                          (step: IExtendedActivityStep, stepIndex: number) => (
                            <IntegrationDetailActivityItemSteps
                              key={stepIndex}
                              duration={step.duration}
                              name={step.name || ''}
                              output={step.output}
                              status={
                                step.isFailed
                                  ? i18n.t('shared:Error')
                                  : i18n.t('shared:Success')
                              }
                            />
                          )
                        )}
                        time={new Date(activity.at).getTime().toLocaleString()}
                        version={activity.version}
                      />
                    )
                  )}
                </>
              );
            }}
          </WithActivities>
        )}
      </Translation>
    );
  }
}
