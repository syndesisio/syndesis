import {
  getSteps,
  setActionOnStep,
  TEMPLATE,
  useIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration, Step, StepKind, StringMap } from '@syndesis/models';
import {
  EditorPageCard,
  IntegrationEditorLayout,
  PageSection,
  TemplateType,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useDropzone } from 'react-dropzone';
import { PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  IPageWithEditorBreadcrumb,
  ITemplateStepRouteParams,
  ITemplateStepRouteState,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import { useTemplater } from './useTemplater';

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

export const TemplateStepPage: React.FunctionComponent<
  ITemplateStepPageProps
> = props => {
  const { addStep, updateStep } = useIntegrationHelpers();
  const { params, state, history } = useRouteData<
    ITemplateStepRouteParams,
    ITemplateStepRouteState
  >();
  const positionAsNumber = parseInt(params.position, 10);
  const fileInput = React.useRef<HTMLInputElement>(null);

  const configuredProperties = state.step.configuredProperties || {};
  const language = configuredProperties.language || TemplateType.Mustache;
  const template = React.useRef(configuredProperties.template || '');

  const handleDropAccepted = React.useCallback(
    (files: File[]) => {
      // just take the last element
      const file = files.pop();
      if (typeof file === 'undefined') {
        return;
      }
      const reader = new FileReader();
      reader.onload = () => {
        const text = reader.result as string;
        template.current = text;
      };
      reader.readAsText(file);
    },
    [template]
  );

  const handleDropRejected = React.useCallback((files: File[]) => {
    const file = files.pop();
    if (typeof file === 'undefined') {
      return;
    }
    // this.setState({
    //   fileErrorMessage: `Could not accept "${file.name}"`,
    // });
  }, []);

  const { getRootProps, getInputProps } = useDropzone({
    disabled: false,
    maxSize: 1024 * 1000,
    multiple: false,
    onDropAccepted: handleDropAccepted,
    onDropRejected: handleDropRejected,
  });

  const onUploadBrowse = React.useCallback(() => {
    fileInput.current!.click();
  }, [fileInput]);

  const onUpdatedIntegration = React.useCallback(
    async ({ action, values }: StringMap<any>) => {
      const updatedIntegration = await (props.mode === 'adding'
        ? addStep
        : updateStep)(
        state.updatedIntegration || state.integration,
        setActionOnStep(state.step as Step, action, TEMPLATE) as StepKind,
        params.flowId,
        positionAsNumber,
        values
      );
      history.push(
        props.postConfigureHref(updatedIntegration, params, {
          ...state,
          updatedIntegration,
        })
      );
    },
    [props, addStep, updateStep, params, state, history, positionAsNumber]
  );

  const { templater, isValid, submitForm } = useTemplater({
    initialLanguage: language as TemplateType,
    initialText: template.current,
    onUpdatedIntegration,
    onUploadBrowse,
  });

  return (
    <>
      <PageTitle title={'Upload Template'} />
      <IntegrationEditorLayout
        title={'Upload Template'}
        description={
          'A template step takes data from a source and inserts it into the format that is defined in a template that you provide.'
        }
        toolbar={props.getBreadcrumb('Upload template', params, state)}
        sidebar={props.sidebar({
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
          <PageSection>
            <div {...getRootProps({ className: 'dropzone' })}>
              <input {...getInputProps()} ref={fileInput} />
              <EditorPageCard
                i18nDone={'Done'}
                isValid={isValid}
                submitForm={submitForm}
              >
                {templater}
              </EditorPageCard>
            </div>
          </PageSection>
        }
        cancelHref={props.cancelHref(params, state)}
      />
    </>
  );
};
