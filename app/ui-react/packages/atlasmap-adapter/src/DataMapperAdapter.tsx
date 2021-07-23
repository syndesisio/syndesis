import * as React from 'react';

import {
  Atlasmap,
  AtlasmapProvider,
  IAtlasmapProviderProps,
  ParametersDialog,
} from '@atlasmap/atlasmap';

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
    // tslint:disable-next-line: no-console
    console.log('Atlasmap document', JSON.stringify(externalDocument));
    return (
      <AtlasmapProvider
        logLevel={'info'}
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

export interface IParameter {
  name: string;
  label: string;
  value: string;
  boolean?: boolean;
  options?: IParameterOption[];
  hidden?: boolean;
  required?: boolean;
}

export const DataShapeParametersDialog: React.FunctionComponent<{
  title: string;
  shown: boolean;
  parameters: IParameter[];
  onConfirm: (parameters: IParameter[]) => void;
  onCancel: () => void;
}> = ({ title, shown, parameters, onConfirm, onCancel }) => {
  return (
    <ParametersDialog
      isOpen={shown}
      title={title}
      onCancel={onCancel}
      onConfirm={onConfirm}
      parameters={parameters}
    />
  );
};
