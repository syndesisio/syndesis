import { WithActivities } from '@syndesis/api';
import {
  Activity,
  ActivityStep,
  IntegrationDeployment,
  Step,
} from '@syndesis/models';
import {
  IntegrationDetailActivity,
  IntegrationDetailActivityItem,
  IntegrationDetailActivityItemSteps,
} from '@syndesis/ui';
import { toDurationString } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';

export interface IActivityPagetableProps {
  integrationId: string;
  linkToOpenShiftLog: string;
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
              fetchActivities,
              fetchDeployments,
            }) => {
              const activities = activitiesBase as IExtendedActivity[];
              const integrationDeployments = (deploymentsBase ||
                []) as IExtendedDeployment[];

              const refresh = async () => {
                await fetchActivities();
                await fetchDeployments();
              };

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
                      ...(step.messages || []),
                      step.failure,
                    ].filter(messages => !!messages);
                    step.output =
                      errorMessages.length > 0 ? errorMessages.join('\n') : '';
                  });
                }
              });
              const lastRefreshed = new Date().toLocaleString();

              return (
                <>
                  <IntegrationDetailActivity
                    i18nBtnRefresh={t('shared:Refresh')}
                    i18nLastRefresh={t('integrations:LastRefresh', {
                      at: lastRefreshed,
                    })}
                    i18nViewLogOpenShift={t(
                      'integrations:View Log in OpenShift'
                    )}
                    linkToOpenShiftLog={this.props.linkToOpenShiftLog}
                    onRefresh={refresh}
                    children={activities.map(
                      (activity: IExtendedActivity, activityIndex: number) => {
                        const date = new Date(
                          activity.at!
                        ).toLocaleDateString();
                        const time = new Date(
                          activity.at!
                        ).toLocaleTimeString();

                        return (
                          <IntegrationDetailActivityItem
                            key={activityIndex}
                            date={date}
                            errorCount={activity.failed ? 1 : 0}
                            i18nErrorsFound={t('integrations:ErrorsFound')}
                            i18nNoErrors={t('integrations:NoErrors')}
                            i18nNoSteps={t('integrations:NoSteps')}
                            i18nVersion={t('shared:Version')}
                            steps={(activity.steps || []).map(
                              (
                                step: IExtendedActivityStep,
                                stepIndex: number
                              ) => (
                                <IntegrationDetailActivityItemSteps
                                  key={stepIndex}
                                  duration={toDurationString(
                                    step.duration!,
                                    'ns'
                                  )}
                                  name={step.name || ''}
                                  output={step.output}
                                  time={new Date(step.at!).toLocaleString()}
                                  status={
                                    step.isFailed
                                      ? t('shared:Error')
                                      : t('shared:Success')
                                  }
                                />
                              )
                            )}
                            time={time}
                            version={activity.ver!}
                          />
                        );
                      }
                    )}
                  />
                </>
              );
            }}
          </WithActivities>
        )}
      </Translation>
    );
  }
}
