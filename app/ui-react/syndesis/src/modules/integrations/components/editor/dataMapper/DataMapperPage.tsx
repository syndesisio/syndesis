import {
  DataShapeKinds,
  getSteps,
  WithIntegrationHelpers,
} from '@syndesis/api';
import { DataMapperAdapter } from '@syndesis/atlasmap-adapter';
import * as H from '@syndesis/history';
import { Action, Integration } from '@syndesis/models';
import { ButtonLink, IntegrationEditorLayout, PageSection } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { AppContext } from '../../../../../app';
import { PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  IDataMapperRouteParams,
  IDataMapperRouteState,
  IPageWithEditorBreadcrumb,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import { getInputDocuments, getOutputDocument } from './utils';

const MAPPING_KEY = 'atlasmapping';

export interface IDataMapperPageProps extends IPageWithEditorBreadcrumb {
  cancelHref: (
    p: IDataMapperRouteParams,
    s: IDataMapperRouteState
  ) => H.LocationDescriptor;
  mode: 'adding' | 'editing';
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  postConfigureHref: (
    integration: Integration,
    p: IDataMapperRouteParams,
    s: IDataMapperRouteState
  ) => H.LocationDescriptorObject;
}

/**
 * This page shows the configuration form for a given action.
 *
 * Submitting the form will update an *existing* integration step in
 * the [position specified in the params]{@link IDataMapperRouteParams#position}
 * of the first flow, set up as specified by the form values.
 *
 * This component expects some [url params]{@link IDataMapperRouteParams}
 * and [state]{@link IDataMapperRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export const DataMapperPage: React.FunctionComponent<
  IDataMapperPageProps
> = props => {
  const appContext = React.useContext(AppContext);
  const [mappings, setMapping] = React.useState<string | undefined>(undefined);

  const onMappings = (newMappings: string) => {
    setMapping(newMappings);
  };

  return (
    <WithIntegrationHelpers>
      {({ addStep, updateStep }) => (
        <WithRouteData<IDataMapperRouteParams, IDataMapperRouteState>>
          {(params, state, { history }) => {
            const positionAsNumber = parseInt(params.position, 10);

            const inputDocuments = getInputDocuments(
              state.integration,
              params.flowId,
              positionAsNumber
            );
            const outputDocument = getOutputDocument(
              state.integration,
              params.flowId,
              positionAsNumber,
              state.step.id!,
              props.mode === 'adding'
            );

            const saveMappingStep = async () => {
              const updatedIntegration = await (props.mode === 'adding'
                ? addStep
                : updateStep)(
                state.integration,
                {
                  ...state.step,
                  action: {
                    actionType: 'step',
                    descriptor: {
                      inputDataShape: {
                        kind: DataShapeKinds.ANY,
                        name: 'All preceding outputs',
                      },
                      outputDataShape: outputDocument.dataShape,
                    } as any,
                  } as Action,
                },
                params.flowId,
                positionAsNumber,
                {
                  [MAPPING_KEY]: mappings,
                }
              );
              history.push(
                props.postConfigureHref(updatedIntegration, params, {
                  ...state,
                  updatedIntegration,
                })
              );
            };

            return (
              <>
                <PageTitle title={state.step.name} />
                <IntegrationEditorLayout
                  title={state.step.name}
                  description={state.step.description}
                  toolbar={props.getBreadcrumb(state.step.name, params, state)}
                  sidebar={props.sidebar({
                    activeIndex: positionAsNumber,
                    activeStep: toUIStep(state.step),
                    initialExpanded: false,
                    steps: toUIStepCollection(
                      getSteps(state.integration, params.flowId)
                    ),
                  })}
                  content={
                    <PageSection
                      noPadding={true}
                      style={{
                        overflow: 'hidden',
                      }}
                    >
                      <DataMapperAdapter
                        documentId={state.integration.id!}
                        inputDocuments={inputDocuments}
                        outputDocument={outputDocument}
                        initialMappings={
                          (state.step.configuredProperties || {})[MAPPING_KEY]
                        }
                        {...appContext.config.datamapper}
                        onMappings={onMappings}
                      />
                    </PageSection>
                  }
                  extraActions={
                    <ButtonLink
                      data-testid={'data-mapper-page-save-mapping-button'}
                      onClick={saveMappingStep}
                      disabled={!mappings}
                      as={'primary'}
                    >
                      Done
                    </ButtonLink>
                  }
                  cancelHref={props.cancelHref(params, state)}
                />
              </>
            );
          }}
        </WithRouteData>
      )}
    </WithIntegrationHelpers>
  );
};
