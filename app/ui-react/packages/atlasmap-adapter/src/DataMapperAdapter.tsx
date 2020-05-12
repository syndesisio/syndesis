import {
  Atlasmap,
  AtlasmapProvider,
  IAtlasmapProviderProps,
} from '@atlasmap/atlasmap';
import * as React from 'react';

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
  baseCSVInspectionServiceUrl?: string;
  baseMappingServiceUrl: string;
  onMappings(mappings: string): void;
}

export const DataMapperAdapter: React.FunctionComponent<IDataMapperAdapterProps> = ({
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
      baseJSONInspectionServiceUrl={baseJSONInspectionServiceUrl}
      baseJavaInspectionServiceUrl={baseJavaInspectionServiceUrl}
      baseMappingServiceUrl={baseMappingServiceUrl}
      baseXMLInspectionServiceUrl={baseXMLInspectionServiceUrl}
      baseCSVInspectionServiceUrl={
        baseCSVInspectionServiceUrl || `${baseMappingServiceUrl}csv/`
      }
      externalDocument={externalDocument}
      onMappingChange={onMappings}
    >
      <Atlasmap
        showImportAtlasFileToolbarItem={false}
        showExportAtlasFileToolbarItem={false}
        showResetToolbarItem={false}
      />
    </AtlasmapProvider>
  );
};
