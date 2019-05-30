import {
  AGGREGATE,
  DataShapeKinds,
  getPreviousIntegrationStepsWithDataShape,
  getSubsequentIntegrationStepsWithDataShape,
  SPLIT,
  toDataShapeKinds,
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

interface IDocumentWithShape extends IDocument {
  dataShape: DataShape;
}

export function stepToProps(
  step: Step,
  isSource: boolean,
  showFields: boolean,
  index: number
): IDocumentWithShape | false {
  const dataShape = isSource
    ? step.action!.descriptor!.outputDataShape!
    : step.action!.descriptor!.inputDataShape!;

  const basicInfo = {
    dataShape,
    description: dataShape.description || '',
    id: step.id!,
    inspectionResult: '',
    inspectionSource: '',
    name:
      index + 1 + ' - ' + (dataShape.name ? dataShape.name : dataShape.type),
    showFields,
  };

  switch (toDataShapeKinds(dataShape.kind!)) {
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
  const splitIndex = previousSteps.reduceRight(
    (foundIndex, s, idx) =>
      s.step.stepKind!.toLowerCase() === stepKind!.toLowerCase() &&
      idx > foundIndex
        ? idx
        : foundIndex,
    -1
  );
  if (splitIndex !== -1) {
    previousSteps = previousSteps.slice(splitIndex);
  }
  return previousSteps;
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
  const allPreviousSteps = getPreviousIntegrationStepsWithDataShape(
    integration,
    flowId,
    position
  )!;

  const previousSteps = restrictPreviousStepArrayScope(
    restrictPreviousStepArrayScope(allPreviousSteps, SPLIT),
    AGGREGATE
  );

  const dataShapeAwareSteps = previousSteps.filter(s =>
    isSupportedDataShape(s.step.action!.descriptor!.outputDataShape!)
  );

  const inputDocuments = dataShapeAwareSteps
    .map(s =>
      stepToProps(s.step, true, dataShapeAwareSteps.length === 1, s.index)
    )
    .filter(s => s) as IDocumentWithShape[];

  return inputDocuments;
}

export function getOutputDocument(
  integration: IntegrationOverview,
  flowId: string,
  position: number,
  stepId: string,
  isAddingStep: boolean
) {
  const subsequentSteps = getSubsequentIntegrationStepsWithDataShape(
    integration,
    flowId,
    isAddingStep ? position - 1 : position
  )!.map(s =>
    isAddingStep
      ? {
          index: s.index + 1,
          step: s.step,
        }
      : s
  );

  const outputDocuments = subsequentSteps
    .map(s => stepToProps(s.step, false, true, s.index))
    .filter(s => s !== false && s.id !== stepId) as IDocumentWithShape[];
  if (outputDocuments.length === 0) {
    throw new Error('output document shape kind not supported');
  }
  return outputDocuments[0];
}
