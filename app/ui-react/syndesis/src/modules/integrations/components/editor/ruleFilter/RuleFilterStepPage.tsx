import {
  getPreviousIntegrationStepWithDataShape,
  getSteps,
  WithFilterOptions,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { DataShape, Integration } from '@syndesis/models';
import {
  IntegrationEditorLayout,
  PageLoader,
  PageSection,
  RuleFilterCard,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { ApiError, PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  IRuleFilterStepRouteParams,
  IRuleFilterStepRouteState,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import {
  IOnUpdatedIntegrationProps,
  WithRuleFilterForm,
} from './WithRuleFilterForm';

export interface IRuleFilterStepPageProps {
  cancelHref: (
    p: IRuleFilterStepRouteParams,
    s: IRuleFilterStepRouteState
  ) => H.LocationDescriptor;
  // tslint:disable-next-line:react-unused-props-and-state
  mode: 'adding' | 'editing';
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  // tslint:disable-next-line:react-unused-props-and-state
  postConfigureHref: (
    integration: Integration,
    p: IRuleFilterStepRouteParams,
    s: IRuleFilterStepRouteState
  ) => H.LocationDescriptorObject;
}

export class RuleFilterStepPage extends React.Component<
  IRuleFilterStepPageProps
> {
  public render() {
    return (
      <>
        <WithIntegrationHelpers>
          {({ addStep, updateStep }) => (
            <WithRouteData<
              IRuleFilterStepRouteParams,
              IRuleFilterStepRouteState
            >>
              {(
                { flowId, position },
                { step, integration, updatedIntegration },
                { history }
              ) => {
                const positionAsNumber = parseInt(position, 10);
                let dataShape = {} as DataShape;
                try {
                  const prevStep = getPreviousIntegrationStepWithDataShape(
                    integration,
                    flowId,
                    positionAsNumber
                  );
                  dataShape =
                    prevStep!.action!.descriptor!.outputDataShape ||
                    ({} as DataShape);
                } catch (err) {
                  // ignore
                }
                const handleSubmitForm = async ({
                  values,
                }: IOnUpdatedIntegrationProps) => {
                  updatedIntegration = await (this.props.mode === 'adding'
                    ? addStep
                    : updateStep)(
                    updatedIntegration || integration,
                    step,
                    flowId,
                    positionAsNumber,
                    values
                  );
                  history.push(
                    this.props.postConfigureHref(
                      updatedIntegration,
                      { flowId, position },
                      {
                        integration,
                        step,
                        updatedIntegration,
                      }
                    )
                  );
                };
                return (
                  <>
                    <PageTitle title={'Basic Filter Configuration'} />
                    <IntegrationEditorLayout
                      title={'Configure Basic Filter Step'}
                      description={
                        'Define one or more rules for evaluating data to determine whether the integration should continue.'
                      }
                      sidebar={this.props.sidebar({
                        activeIndex: positionAsNumber,
                        activeStep: toUIStep(step),
                        steps: toUIStepCollection(
                          getSteps(updatedIntegration || integration, flowId)
                        ),
                      })}
                      content={
                        <WithFilterOptions dataShape={dataShape}>
                          {({ data, error, hasData }) => (
                            <WithLoader
                              error={error}
                              loading={!hasData}
                              loaderChildren={<PageLoader />}
                              errorChildren={
                                <PageSection>
                                  <ApiError />
                                </PageSection>
                              }
                            >
                              {() => (
                                <WithRuleFilterForm
                                  step={step}
                                  filterOptions={data}
                                  onUpdatedIntegration={handleSubmitForm}
                                >
                                  {({ form, isValid, submitForm }) => (
                                    <>
                                      <RuleFilterCard
                                        i18nDone={'Done'}
                                        isValid={isValid}
                                        submitForm={submitForm}
                                      >
                                        {form}
                                      </RuleFilterCard>
                                    </>
                                  )}
                                </WithRuleFilterForm>
                              )}
                            </WithLoader>
                          )}
                        </WithFilterOptions>
                      }
                      cancelHref={this.props.cancelHref(
                        { flowId, position },
                        {
                          integration,
                          step,
                          updatedIntegration,
                        }
                      )}
                    />
                  </>
                );
              }}
            </WithRouteData>
          )}
        </WithIntegrationHelpers>
      </>
    );
  }
}
