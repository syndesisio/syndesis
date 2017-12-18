import { BaseEntity } from '@syndesis/ui/model';
import { StringMap, BaseReducerModel } from '@syndesis/ui/platform';

export interface ApiConnector extends BaseEntity {
  kind: string;
  data: ApiConnectorData;
}

//export type ApiConnectors = Array<ApiConnector>;

export interface ApiConnectorData extends BaseEntity, ApiConnectorValidation {
  id?: string;
  name: string;
  description: string;
  icon: string;
  fileIcon?: File;
  properties: ApiConnectorProperties;
  connectorProperties: any;
}

export type ApiConnectors = Array<ApiConnectorData>;

export interface ApiConnectorProperties extends BaseEntity {
  specification: {
    kind: string;
    displayName: string;
    required: boolean;
    type: string;
    javaType: string;
    description: string
  };
  specificationUrl: {
    kind: string;
    displayName: string;
    required: boolean;
    type: string;
    javaType: string;
    description: string;
  };
}

// XXX: The following models are designed to fit into the Redux container
//      and will eventually replace the ones above

export interface ApiConnectorValidation {
  validationDetails: {
    actionsSummary?: {
      actionCountByTags: StringMap<number>;
      totalActions: number;
    };
    warnings: Array<{ key: string; longdesc: string; }>;
    errors: Array<{ key: string; longdesc: string; }>;
  };
}

export interface CustomApiConnectorRequest extends BaseReducerModel {
  connectorTemplateId: string;
  name?: string;
  description?: string;
  icon: string;
}

export interface CustomSwaggerConnectorRequest extends CustomApiConnectorRequest, ApiConnectorValidation {
  configuredProperties: {
    specification?: string;
    host: string;
    basePath: string;
    authentication: any;
  };
  file: File;
}
