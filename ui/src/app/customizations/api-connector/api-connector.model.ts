import { BaseEntity } from '../../model';

export interface ApiConnector extends BaseEntity {
  kind: string;
  data: {};
}
export type ApiConnectors = Array<ApiConnector>;


export interface ApiConnectorData extends BaseEntity {
  id: string,
  name: string,
  description: string,
  icon: string,
  properties: ApiConnectorProperties,
  connectorProperties: {}
}

export interface ApiConnectorProperties extends BaseEntity {
  specification: {
    kind: string,
    displayName: string,
    required: boolean,
    type: string,
    javaType: string,
    //tags: Array<Tag>,
    description: string
  },
  specificationUrl: {
    kind: string,
    displayName: string,
    required: boolean,
    type: string,
    javaType: string,
    description: string
  }
}

class TypeFactoryClass {
  createApiConnector() {
    return <ApiConnector>{
      kind: undefined,
      data: undefined
    };
  }
}

export const TypeFactory = new TypeFactoryClass();

