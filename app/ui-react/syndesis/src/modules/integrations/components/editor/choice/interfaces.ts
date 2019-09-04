/**
 * Interface that represents a conditional flow step's configured properties
 */
export interface IChoiceConfiguration {
  routingScheme: string;
  defaultFlow?: string;
  defaultFlowEnabled: boolean;
  flows: IFlowOption[];
}

export interface IFlowOption {
  name?: string;
  flow: string;
  condition?: string;
  path?: string;
  op?: string;
  value?: string;
}

/**
 * Interfaces that represents what the conditional flow form expects
 */
export interface IChoiceFormConfiguration {
  defaultFlowId: string;
  useDefaultFlow: boolean;
  flowConditions: IFlowFormOption[];
  routingScheme: string;
}

export interface IFlowFormOption {
  flowId: string;
  condition?: string;
  path?: string;
  op?: string;
  value?: string;
}
