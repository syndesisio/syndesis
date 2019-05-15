import { getSteps, WithIntegrationHelpers } from '@syndesis/api';
import { DataMapperAdapter } from '@syndesis/atlasmap-adapter';
import * as H from '@syndesis/history';
import { Integration } from '@syndesis/models';
import { IntegrationEditorLayout, PageSection } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import { IDataMapperRouteParams, IDataMapperRouteState } from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';
import { getInputDocuments, getOutputDocument } from './utils';

const MAPPING_KEY = 'atlasmapping';

export interface IDataMapperPageProps {
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
  const [mappings, setMapping] = React.useState<string | undefined>(undefined);

  const onMappings = (newMappings: string) => {
    // tslint:disable-next-line
    console.log('onMappings', newMappings, mappings);
    setMapping(newMappings);
  };

  return (
    <WithIntegrationHelpers>
      {({ addStep, updateStep }) => (
        <WithRouteData<IDataMapperRouteParams, IDataMapperRouteState>>
          {(
            { flowId, position },
            { step, integration, updatedIntegration },
            { history }
          ) => {
            const positionAsNumber = parseInt(position, 10);

            const inputDocuments = getInputDocuments(
              integration,
              flowId,
              positionAsNumber
            );
            const outputDocument = getOutputDocument(
              integration,
              flowId,
              props.mode === 'adding' ? positionAsNumber - 1 : positionAsNumber,
              step.id!
            );

            return (
              <>
                <PageTitle title={step.name} />
                <IntegrationEditorLayout
                  title={step.name}
                  description={step.description}
                  sidebar={props.sidebar({
                    activeIndex: positionAsNumber,
                    activeStep: toUIStep(step),
                    initialExpanded: false,
                    steps: toUIStepCollection(
                      getSteps(updatedIntegration || integration, flowId)
                    ),
                  })}
                  content={
                    <PageSection noPadding={true}>
                      <DataMapperAdapter
                        documentId={integration.id!}
                        inputDocuments={inputDocuments}
                        outputDocument={outputDocument}
                        initialMappings={
                          (step.configuredProperties || {})[MAPPING_KEY]
                        }
                        onMappings={onMappings}
                      />
                    </PageSection>
                  }
                  cancelHref={props.cancelHref(
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
};
