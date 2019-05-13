import { getSteps, WithIntegrationHelpers } from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration, StringMap } from '@syndesis/models';
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
  ITemplateStepRouteParams,
  ITemplateStepRouteState,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import { WithTemplater } from './WithTemplater';

export interface ITemplateStepPageProps {
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
            {(
              { flowId, position },
              { step, integration, updatedIntegration },
              { history }
            ) => {
              const positionAsNumber = parseInt(position, 10);
              const onUpdatedIntegration = async ({
                values,
              }: StringMap<any>) => {
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
              const configuredProperties = step.configuredProperties || {};
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
                    sidebar={this.props.sidebar({
                      activeIndex: positionAsNumber,
                      activeStep: toUIStep(step),
                      steps: toUIStepCollection(
                        getSteps(updatedIntegration || integration, flowId)
                      ),
                    })}
                    content={
                      <WithTemplater
                        initialLanguage={language as TemplateType}
                        initialText={template}
                        onUpdatedIntegration={onUpdatedIntegration}
                      >
                        {({ controls, submitForm }) => (
                          <TemplateStepCard
                            i18nDone={'Done'}
                            isValid={true}
                            submitForm={submitForm}
                          >
                            {controls}
                          </TemplateStepCard>
                        )}
                      </WithTemplater>
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
    );
  }
}
