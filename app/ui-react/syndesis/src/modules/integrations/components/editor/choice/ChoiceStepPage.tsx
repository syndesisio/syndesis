import {
  getSteps,
  WithConnection,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration } from '@syndesis/models';
import {
  ChoiceCardHeader,
  ChoiceViewMode,
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
  IChoiceStepRouteParams,
  IChoiceStepRouteState,
  IPageWithEditorBreadcrumb,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import { IChoiceFormConfiguration } from './interfaces';
import { createChoiceConfiguration } from './utils';
import { WithChoiceConfigurationForm } from './WithChoiceConfigurationForm';

export interface IChoiceStepPageProps extends IPageWithEditorBreadcrumb {
  cancelHref: (
    p: IChoiceStepRouteParams,
    s: IChoiceStepRouteState
  ) => H.LocationDescriptor;
  // tslint:disable-next-line:react-unused-props-and-state
  mode: 'adding' | 'editing';
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  // tslint:disable-next-line:react-unused-props-and-state
  postConfigureHref: (
    integration: Integration,
    p: IChoiceStepRouteParams,
    s: IChoiceStepRouteState
  ) => H.LocationDescriptorObject;
}

export interface IChoiceStepPageState {
  mode: 'view' | 'edit';
}

export class ChoiceStepPage extends React.Component<
  IChoiceStepPageProps,
  IChoiceStepPageState
> {
  constructor(props: IChoiceStepPageProps) {
    super(props);
    this.state = {
      mode: this.props.mode === 'adding' ? 'edit' : 'view',
    };
    this.handleSetMode = this.handleSetMode.bind(this);
  }
  public handleSetMode(mode: 'view' | 'edit') {
    this.setState({ mode });
  }
  public render() {
    return (
      <WithConnection id={'flow'}>
        {({ data, error, hasData }) => (
          <WithIntegrationHelpers>
            {({ addStep, updateStep }) => (
              <WithRouteData<IChoiceStepRouteParams, IChoiceStepRouteState>>
                {(params, state, { history }) => {
                  const positionAsNumber = parseInt(params.position, 10);
                  const step = state.step;
                  // parse the configured properties
                  const configuration = createChoiceConfiguration(
                    step.configuredProperties || {}
                  );
                  // create the values displayed in the form
                  const initialFormValue = {
                    flowConditions: configuration.flows.map(
                      ({ condition, flow }) => ({
                        condition,
                        flowId: flow,
                      })
                    ),
                    routingScheme: configuration.routingScheme,
                    useDefaultFlow: configuration.defaultFlowEnabled,
                  };
                  // create links
                  const flowItems = configuration.flows.map(
                    ({ condition, flow }) => ({
                      condition,
                      href: '' /* todo */,
                    })
                  );
                  const defaultFlowHref = configuration.defaultFlowEnabled
                    ? '' /* todo */
                    : undefined;
                  const onUpdatedIntegration = async (
                    values: IChoiceFormConfiguration
                  ) => {
                    /* todo */
                  };
                  return (
                    <>
                      <PageTitle title={'Configure Conditional Flows'} />
                      <IntegrationEditorLayout
                        title={'Configure Conditional Flows'}
                        description={
                          'Define one to many conditions in order to route messages to different flows.'
                        }
                        toolbar={this.props.getBreadcrumb(
                          'Configure Conditional Flows',
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
                              <WithChoiceConfigurationForm
                                initialValue={initialFormValue}
                                onUpdatedIntegration={onUpdatedIntegration}
                                stepId={step.id!}
                              >
                                {({ fields, isValid, submitForm }) => (
                                  <EditorPageCard
                                    header={
                                      <ChoiceCardHeader
                                        i18nConditions={'Conditions'}
                                        i18nManage={'Manage'}
                                        i18nApply={'Apply'}
                                        isValid={isValid}
                                        mode={this.state.mode}
                                        onClickManage={() =>
                                          this.handleSetMode('edit')
                                        }
                                        onClickApply={() => {
                                          submitForm();
                                          this.handleSetMode('view');
                                        }}
                                      />
                                    }
                                    i18nDone={'Done'}
                                    isValid={
                                      this.state.mode === 'view' ||
                                      (this.state.mode === 'edit' && isValid)
                                    }
                                    submitForm={submitForm}
                                  >
                                    {this.state.mode === 'view' && (
                                      <ChoiceViewMode
                                        flowItems={flowItems}
                                        useDefaultFlow={
                                          configuration.defaultFlowEnabled
                                        }
                                        defaultFlowHref={defaultFlowHref}
                                        i18nWhen={'When'}
                                        i18nOtherwise={'Otherwise'}
                                        i18nOpenFlow={'Open Flow'}
                                        i18nUseDefaultFlow={
                                          'Use a default flow'
                                        }
                                      />
                                    )}
                                    {this.state.mode === 'edit' && fields}
                                  </EditorPageCard>
                                )}
                              </WithChoiceConfigurationForm>
                            )}
                          </WithLoader>
                        }
                        cancelHref={this.props.cancelHref(params, state)}
                      />
                    </>
                  );
                }}
              </WithRouteData>
            )}
          </WithIntegrationHelpers>
        )}
      </WithConnection>
    );
  }
}
