import { BaseEntity } from '@syndesis/ui/model';
import { StringMap } from '@syndesis/ui/platform';

export interface ApiConnector extends BaseEntity {
  kind: string;
  data: ApiConnectorData;
}

export type ApiConnectors = Array<ApiConnector>;

export interface ApiConnectorData extends BaseEntity, ApiConnectorValidation {
  id: string;
  name: string;
  description: string;
  icon: string;
  properties: ApiConnectorProperties;
  connectorProperties: any;
}

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
