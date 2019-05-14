import {
  DataShapeKinds,
  getSteps,
  getPreviousIntegrationStepWithDataShape,
  getSubsequentIntegrationStepWithDataShape,
  WithIntegrationHelpers,
} from '@syndesis/api';
import {
  DataMapperAdapter,
  DocumentType,
  InspectionType,
} from '@syndesis/atlasmap-adapter';
import * as H from '@syndesis/history';
import { Integration, Step } from '@syndesis/models';
import { IntegrationEditorLayout, PageSection } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import { IDataMapperRouteParams, IDataMapperRouteState } from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';

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
  //
  // const onReset = () => {
  //   console.log('onReset');
  //   setMapping(undefined);
  // }

  const onMappings = (newMappings: string) => {
    console.log('onMappings', newMappings);
    setMapping(newMappings);
  };

  const stepToProps = (step: Step, isSource: boolean) => {
    const dataShape = isSource
      ? step.action!.descriptor!.outputDataShape!
      : step.action!.descriptor!.inputDataShape!;

    const basicInfo = {
      description: dataShape.description || '',
      name: step.name!,
      specification: dataShape.specification!,
    };

    switch (dataShape.kind!.toLowerCase()) {
      case DataShapeKinds.JAVA:
        return {
          ...basicInfo,
          documentType: DocumentType.JAVA,
          inspectionResult: dataShape.specification,
          inspectionSource: dataShape.type,
          inspectionType: InspectionType.JAVA_CLASS,
        };
      case DataShapeKinds.JSON_INSTANCE:
        return {
          ...basicInfo,
          documentType: DocumentType.JSON,
          inspectionSource: dataShape.specification,
          inspectionType: InspectionType.INSTANCE,
        };
      case DataShapeKinds.JSON_SCHEMA:
        return {
          ...basicInfo,
          documentType: DocumentType.JSON,
          inspectionSource: dataShape.specification,
          inspectionType: InspectionType.SCHEMA,
        };
      case DataShapeKinds.XML_INSTANCE:
        return {
          ...basicInfo,
          documentType: DocumentType.XML,
          inspectionSource: dataShape.specification,
          inspectionType: InspectionType.INSTANCE,
        };
      case DataShapeKinds.XML_SCHEMA:
        return {
          ...basicInfo,
          documentType: DocumentType.XML,
          inspectionSource: dataShape.specification,
          inspectionType: InspectionType.SCHEMA,
        };
      case DataShapeKinds.XML_SCHEMA_INSPECTED:
        return {
          ...basicInfo,
          documentType: DocumentType.XML,
          inspectionResult: dataShape.specification,
          inspectionType: InspectionType.SCHEMA,
        };
      default:
        throw new Error('unsupported data shape kind');
    }
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
            const previousStep = getPreviousIntegrationStepWithDataShape(
              integration,
              flowId,
              positionAsNumber - 2
            )!;
            const subsequentStep = getSubsequentIntegrationStepWithDataShape(
              integration,
              flowId,
              positionAsNumber - 1
            )!;

            console.log('prev', previousStep);
            console.log('sub', subsequentStep);

            const inputProps = stepToProps(previousStep, true);
            const outputProps = stepToProps(subsequentStep, false);

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
                        inputName={inputProps.name}
                        inputDescription={inputProps.description}
                        inputDocumentType={inputProps.documentType}
                        inputInspectionType={inputProps.inspectionType}
                        inputDataShape={inputProps.specification}
                        outputName={outputProps.name}
                        outputDescription={outputProps.description}
                        outputDocumentType={outputProps.documentType}
                        outputInspectionType={outputProps.inspectionType}
                        outputDataShape={outputProps.specification}
                        mappings={mappings}
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
