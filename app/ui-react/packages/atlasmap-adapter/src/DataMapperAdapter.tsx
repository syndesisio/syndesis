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
  // Clicking Cancel in the AtlasMap parameters dialog results in the reset of the internal
  // state of the dialog in such a way that the parameters selected by the user are lost,
  // or at least not shown. For example if the user selects "Skip Header Record" and ticks
  // it so it's value is set to `true`, by clicking on "Confirm" a single parameter for
  // "Skip Header Record" is provided on `onConfirm` callback with the side effect of
  // changing the internal state in `definedParameters`, on "onCancel" callback
  // resets the internal state in `definedParameters` to initial state loosing all user
  // entered values.
  // As a workaround, we're mixing the initial parameters with the selected parameters
  // so that we can keep the state of `definedParameters` as expected for the dialog
  // to present all possible values and keep the user selected values.
  // The trick is in setting the unexported property `enabled` which is consulted in
  // the `reset` function in handling of cancel.
  // Ref. https://github.com/atlasmap/atlasmap/issues/2990
  const computeDialogParameters = (
    given?: IParameters
  ): IParameterDefinition[] => {
    if (given === undefined) {
      return [];
    }

    return parameterDefinition.map((defn) => {
      if (defn.name in given) {
        defn.value = given[defn.name];
        // dirty trick to get `reset` within ParametersDialog not to reset the
        // state of `definedParameters`
        defn.enabled = true;
      }

      return defn;
    });
  };

  // we wish to maintain the interface between usage of DataShapeParametersDialog
  // and AtlasMap, and hide any idiosyncrasies, to `onConfirm` we wish to provide
  // only key-value IParameters choosen by the user, while maintaining the state
  // of ParametersDialog in AtlasMap, as noted above
  const handleConfirm = (given: IParameterDefinition[]) => {
    const newParameters = given.reduce((givenParams, givenParam) => {
      givenParams[givenParam.name] = givenParam.value;
      return givenParams;
    }, {});
    setParams(computeDialogParameters(newParameters));
    onConfirm(newParameters);
  };

  const [params, setParams] = React.useState(
    computeDialogParameters(parameters)
  );

  return (
    <ParametersDialog
      isOpen={shown}
      title={title}
      onCancel={onCancel}
      onConfirm={handleConfirm}
      parameters={params.length === 0 ? parameterDefinition : params}
    />
  );
};

export const atlasmapCSVParameterOptions = getCsvParameterOptions;
