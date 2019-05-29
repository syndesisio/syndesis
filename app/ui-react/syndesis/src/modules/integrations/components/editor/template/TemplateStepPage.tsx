import {
  getSteps,
  setActionOnStep,
  TEMPLATE,
  WithIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration, Step, StepKind, StringMap } from '@syndesis/models';
import {
  IntegrationEditorLayout,
  TemplateStepCard,
  TemplateType,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  IPageWithEditorBreadcrumb,
  ITemplateStepRouteParams,
  ITemplateStepRouteState,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import { WithTemplater } from './WithTemplater';

export interface ITemplateStepPageProps extends IPageWithEditorBreadcrumb {
  mode: 'adding' | 'editing';
  cancelHref: (
    p: ITemplateStepRouteParams,
    s: ITemplateStepRouteState
  ) => H.LocationDescriptor;
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  postConfigureHref: (
    integration: Integration,
    p: ITemplateStepRouteParams,
    s: ITemplateStepRouteState
  ) => H.LocationDescriptorObject;
}

export class TemplateStepPage extends React.Component<ITemplateStepPageProps> {
  public render() {
    return (
      <WithIntegrationHelpers>
        {({ addStep, updateStep }) => (
          <WithRouteData<ITemplateStepRouteParams, ITemplateStepRouteState>>
            {(params, state, { history }) => {
              const positionAsNumber = parseInt(params.position, 10);
              let isValid = true;
              const handleUpdateLinting = (
                unsortedAnnotations: any[],
                annotations: any[]
              ) => {
                isValid = annotations.length === 0;
              };
              const onUpdatedIntegration = async ({
                action,
                values,
              }: StringMap<any>) => {
                const updatedIntegration = await (this.props.mode === 'adding'
                  ? addStep
                  : updateStep)(
                  state.updatedIntegration || state.integration,
                  setActionOnStep(
                    state.step as Step,
                    action,
                    TEMPLATE
                  ) as StepKind,
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
              const configuredProperties =
                state.step.configuredProperties || {};
              const language =
                configuredProperties.language || TemplateType.Mustache;
              const template = configuredProperties.template || '';
              return (
                <>
                  <PageTitle title={'Upload Template'} />
                  <IntegrationEditorLayout
                    title={'Upload Template'}
                    description={
                      'A template step takes data from a source and inserts it into the format that is defined in a template that you provide.'
                    }
                    toolbar={this.props.getBreadcrumb(
                      'Upload template',
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
                      <WithTemplater
                        initialLanguage={language as TemplateType}
                        initialText={template}
                        onUpdatedIntegration={onUpdatedIntegration}
                        onUpdateLinting={handleUpdateLinting}
                      >
                        {({ controls, submitForm }) => (
                          <TemplateStepCard
                            i18nDone={'Done'}
                            isValid={isValid}
                            submitForm={submitForm}
                          >
                            {controls}
                          </TemplateStepCard>
                        )}
                      </WithTemplater>
                    }
                    cancelHref={this.props.cancelHref(params, state)}
                  />
                </>
              );
            }}
          </WithRouteData>
        )}
      </WithIntegrationHelpers>
    );
  }
}
