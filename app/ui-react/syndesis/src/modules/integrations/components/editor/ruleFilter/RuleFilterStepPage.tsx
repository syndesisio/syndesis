import {
  getOutputDataShapeFromPreviousStep,
  getSteps,
  WithFilterOptions,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration } from '@syndesis/models';
import {
  EditorPageCard,
  IntegrationEditorLayout,
  PageLoader,
  PageSection,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { ApiError, PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  IPageWithEditorBreadcrumb,
  IRuleFilterStepRouteParams,
  IRuleFilterStepRouteState,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import {
  IOnUpdatedIntegrationProps,
  WithRuleFilterForm,
} from './WithRuleFilterForm';

export interface IRuleFilterStepPageProps extends IPageWithEditorBreadcrumb {
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
              {(params, state, { history }) => {
                const positionAsNumber = parseInt(params.position, 10);
                const dataShape = getOutputDataShapeFromPreviousStep(state.integration, params.flowId, positionAsNumber);
                const handleSubmitForm = async ({
                  values,
                }: IOnUpdatedIntegrationProps) => {
                  const updatedIntegration = await (this.props.mode === 'adding'
                    ? addStep
                    : updateStep)(
                    state.updatedIntegration || state.integration,
                    state.step,
                    params.flowId,
                    positionAsNumber,
                    values
                  );
                  history.push(
                    this.props.postConfigureHref(updatedIntegration, params, {
                      ...state,
                      updatedIntegration,
                    })
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
                      toolbar={this.props.getBreadcrumb(
                        'Configure basic filter step',
                        params,
                        state
                      )}
                      sidebar={this.props.sidebar({
                        activeIndex: positionAsNumber,
                        activeStep: toUIStep(state.step),
                        steps: toUIStepCollection(
                          getSteps(
                            state.updatedIntegration || state.integration,
                            params.flowId
                          )
                        ),
                      })}
                      content={
                        <WithFilterOptions dataShape={dataShape}>
                          {({ data, error, errorMessage, hasData }) => (
                            <WithLoader
                              error={error}
                              loading={!hasData}
                              loaderChildren={<PageLoader />}
                              errorChildren={
                                <PageSection>
                                  <ApiError error={errorMessage!} />
                                </PageSection>
                              }
                            >
                              {() => (
                                <WithRuleFilterForm
                                  step={state.step}
                                  filterOptions={data}
                                  onUpdatedIntegration={handleSubmitForm}
                                >
                                  {({ form, isValid, submitForm }) => (
                                    <>
                                      <EditorPageCard
                                        i18nDone={'Done'}
                                        isValid={isValid}
                                        submitForm={submitForm}
                                      >
                                        {form}
                                      </EditorPageCard>
                                    </>
                                  )}
                                </WithRuleFilterForm>
                              )}
                            </WithLoader>
                          )}
                        </WithFilterOptions>
                      }
                      cancelHref={this.props.cancelHref(params, state)}
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
