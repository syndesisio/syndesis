import * as React from 'react';

import {
  Atlasmap,
  AtlasmapProvider,
  IAtlasmapProviderProps,
  ParametersDialog,
} from '@atlasmap/atlasmap';
import { getCsvParameterOptions } from '@atlasmap/core';

export enum DocumentType {
  JAVA = 'JAVA',
  JAVA_ARCHIVE = 'JAR',
  XML = 'XML',
  XSD = 'XSD',
  JSON = 'JSON',
  CORE = 'Core',
  CSV = 'CSV',
  CONSTANT = 'Constants',
  PROPERTY = 'Property',
}
export enum InspectionType {
  JAVA_CLASS = 'JAVA_CLASS',
  SCHEMA = 'SCHEMA',
  INSTANCE = 'INSTANCE',
  UNKNOWN = 'UNKNOWN',
}
export interface IDocument {
  id: string;
  name: string;
  description: string;
  documentType: DocumentType;
  inspectionType: InspectionType;
  inspectionSource: string;
  inspectionResult: string;
  inspectionParameters?: { [key: string]: string };
  showFields: boolean;
}

export interface IDataMapperAdapterProps {
  documentId: string;
  inputDocuments: IDocument[];
  outputDocument: IDocument;
  initialMappings?: string;
  baseJavaInspectionServiceUrl: string;
  baseXMLInspectionServiceUrl: string;
  baseJSONInspectionServiceUrl: string;
  baseCSVInspectionServiceUrl: string;
  baseMappingServiceUrl: string;
  onMappings(mappings: string): void;
}

export interface IParameter {
  name: string;
  label: string;
  value: string;
  boolean?: boolean;
  options?: IParameterOption[];
  enabled?: boolean;
  required?: boolean;
}

export const DataMapperAdapter: React.FunctionComponent<IDataMapperAdapterProps> =
  ({
    documentId,
    inputDocuments,
    outputDocument,
    initialMappings,
    baseJavaInspectionServiceUrl,
    baseXMLInspectionServiceUrl,
    baseJSONInspectionServiceUrl,
    baseCSVInspectionServiceUrl,
    baseMappingServiceUrl,
    onMappings,
  }) => {
    const externalDocument = React.useMemo(
      () =>
        ({
          documentId,
          initialMappings,
          inputDocuments,
          outputDocument,
        } as IAtlasmapProviderProps['externalDocument']),
      [initialMappings, documentId, inputDocuments, outputDocument]
    );
    return (
      <AtlasmapProvider
        logLevel={'warn'}
        baseJSONInspectionServiceUrl={baseJSONInspectionServiceUrl}
        baseJavaInspectionServiceUrl={baseJavaInspectionServiceUrl}
        baseMappingServiceUrl={baseMappingServiceUrl}
        baseXMLInspectionServiceUrl={baseXMLInspectionServiceUrl}
        baseCSVInspectionServiceUrl={baseCSVInspectionServiceUrl}
        externalDocument={externalDocument}
        onMappingChange={onMappings}
      >
        <Atlasmap
          allowImport={false}
          allowExport={false}
          allowReset={false}
          allowDelete={false}
          allowCustomJavaClasses={false}
        />
      </AtlasmapProvider>
    );
  };

export interface IParameterOption {
  label: string;
  value: string;
}

export interface IParameterDefinition {
  name: string;
  label: string;
  value: string;
  boolean?: boolean;
  options?: IParameterOption[];
  hidden?: boolean;
  required?: boolean;
  enabled?: boolean;
}

export interface IParameters {
  [name: string]: string;
}

export const DataShapeParametersDialog: React.FunctionComponent<{
  title: string;
  shown: boolean;
  parameterDefinition: IParameterDefinition[];
  parameters?: IParameters;
  onConfirm: (parameters: IParameters) => void;
  onCancel: () => void;
}> = ({
  title,
  shown,
  parameterDefinition,
  parameters,
  onConfirm,
  onCancel,
}) => {
  const parametersToParameterArray = (given?: IParameters): IParameter[] => {
    if (given === undefined) {
      return [];
    }

    return parameterDefinition.reduce((acc, defn) => {
      if (defn.name in given) {
        acc.push({ ...defn, value: given[defn.name] });
      }

      return acc;
    }, [] as IParameter[]);
  };

  const parameterArrayToParams = (given: IParameter[]): IParameters => {
    return given.reduce((acc, param) => {
      acc[param.name] = param.value;

      return acc;
    }, {});
  };

  // we wish to maintain the interface between usage of DataShapeParametersDialog
  // and AtlasMap, and hide any idiosyncrasies, to `onConfirm` we wish to provide
  // only key-value IParameters choosen by the user, while maintaining the state
  // of ParametersDialog in AtlasMap, as noted above
  const handleConfirm = (given: IParameter[]) => {
    setParams(parameterArrayToParams(given));
    onConfirm(parameterArrayToParams(given));
  };

  const [params, setParams] = React.useState(parameters);

  return (
    <ParametersDialog
      isOpen={shown}
      title={title}
      onCancel={onCancel}
      onConfirm={handleConfirm}
      initialParameters={parametersToParameterArray(params)}
      parameters={parameterDefinition}
    />
  );
};

export const atlasmapCSVParameterOptions = getCsvParameterOptions;
