import {
  AGGREGATE,
  DataShapeKinds,
  getPreviousIntegrationStepsWithDataShape,
  getSubsequentIntegrationStepsWithDataShape,
  SPLIT,
} from '@syndesis/api';
import {
  DocumentType,
  IDocument,
  InspectionType,
} from '@syndesis/atlasmap-adapter';
import {
  DataShape,
  IndexedStep,
  IntegrationOverview,
  Step,
} from '@syndesis/models';

export function stepToProps(
  step: Step,
  isSource: boolean,
  showFields: boolean,
  index: number
): IDocument | false {
  const dataShape = isSource
    ? step.action!.descriptor!.outputDataShape!
    : step.action!.descriptor!.inputDataShape!;

  const basicInfo = {
    description: dataShape.description || '',
    id: step.id!,
    inspectionResult: '',
    inspectionSource: '',
    name:
      index + 1 + ' - ' + (dataShape.name ? dataShape.name : dataShape.type),
    showFields,
  };

  switch (dataShape.kind!.toLowerCase()) {
    case DataShapeKinds.JAVA:
      return {
        ...basicInfo,
        documentType: DocumentType.JAVA,
        inspectionResult: dataShape.specification!,
        inspectionSource: dataShape.type!,
        inspectionType: InspectionType.JAVA_CLASS,
      };
    case DataShapeKinds.JSON_INSTANCE:
      return {
        ...basicInfo,
        documentType: DocumentType.JSON,
        inspectionSource: dataShape.specification!,
        inspectionType: InspectionType.INSTANCE,
      };
    case DataShapeKinds.JSON_SCHEMA:
      return {
        ...basicInfo,
        documentType: DocumentType.JSON,
        inspectionSource: dataShape.specification!,
        inspectionType: InspectionType.SCHEMA,
      };
    case DataShapeKinds.XML_INSTANCE:
      return {
        ...basicInfo,
        documentType: DocumentType.XML,
        inspectionSource: dataShape.specification!,
        inspectionType: InspectionType.INSTANCE,
      };
    case DataShapeKinds.XML_SCHEMA:
      return {
        ...basicInfo,
        documentType: DocumentType.XML,
        inspectionSource: dataShape.specification!,
        inspectionType: InspectionType.SCHEMA,
      };
    case DataShapeKinds.XML_SCHEMA_INSPECTED:
      return {
        ...basicInfo,
        documentType: DocumentType.XML,
        inspectionResult: dataShape.specification!,
        inspectionType: InspectionType.SCHEMA,
      };
    default:
      return false;
  }
}

export function restrictPreviousStepArrayScope(
  previousSteps: IndexedStep[],
  stepKind: Step['stepKind']
): IndexedStep[] {
  const splitIndex = previousSteps
    .reverse()
    .findIndex(s => s.step.stepKind!.toLowerCase() === stepKind!.toLowerCase());

  previousSteps.reverse();

  if (splitIndex !== -1) {
    previousSteps = previousSteps.slice(0, splitIndex);
  }
  return previousSteps.sort((a, b) => a.index - b.index);
}

export function isSupportedDataShape(dataShape: DataShape): boolean {
  if (!dataShape || !dataShape.kind) {
    return false;
  }
  return (
    [
      DataShapeKinds.JAVA,
      DataShapeKinds.JSON_INSTANCE,
      DataShapeKinds.JSON_SCHEMA,
      DataShapeKinds.XML_INSTANCE,
      DataShapeKinds.XML_SCHEMA,
      DataShapeKinds.XML_SCHEMA_INSPECTED,
    ]
      .map(k => k.toUpperCase())
      .indexOf(dataShape.kind.toUpperCase()) > -1
  );
}

export function getInputDocuments(
  integration: IntegrationOverview,
  flowId: string,
  position: number
) {
  const previousSteps = restrictPreviousStepArrayScope(
    restrictPreviousStepArrayScope(
      getPreviousIntegrationStepsWithDataShape(integration, flowId, position)!,
      SPLIT
    ),
    AGGREGATE
  );

  const dataShapeAwareSteps = previousSteps.filter(s =>
    isSupportedDataShape(s.step.action!.descriptor!.outputDataShape!)
  );

  const inputDocuments = dataShapeAwareSteps
    .map(s =>
      stepToProps(s.step, true, dataShapeAwareSteps.length === 1, s.index)
    )
    .filter(s => s) as IDocument[];

  if (inputDocuments.length === 0) {
    throw new Error('input documents shape kind not supported');
  }

  return inputDocuments;
}

export function getOutputDocument(
  integration: IntegrationOverview,
  flowId: string,
  position: number,
  stepId: string
) {
  const subsequentSteps = getSubsequentIntegrationStepsWithDataShape(
    integration,
    flowId,
    position
  )!;

  const outputDocuments = subsequentSteps
    .map(s => stepToProps(s.step, false, true, s.index))
    .filter(s => s !== false && s.id !== stepId) as IDocument[];

  if (outputDocuments.length > 1) {
    throw new Error('output document shape kind not supported');
  }

  return outputDocuments[0];
}
