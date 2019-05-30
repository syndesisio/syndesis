/**
 * Interface that represents a conditional flow step's configured properties
 */
import { Connection, StepKind } from '@syndesis/models';

export interface IChoiceConfiguration {
  routingScheme: string;
  defaultFlow?: string;
  defaultFlowEnabled: boolean;
  flows: IFlowOption[];
}

export interface IFlowOption {
  name?: string;
  flow: string;
  condition: string;
}

/**
 * Interfaces that represents what the conditional flow form expects
 */
export interface IChoiceFormConfiguration {
  useDefaultFlow: boolean;
  flowConditions: IFlowFormOption[];
  routingScheme: string;
}

export interface IFlowFormOption {
  flowId: string;
  condition: string;
}

export interface ICreateFlowProps {
  name: string;
  kind: string;
  description: string;
  primaryFlowId: string;
  flowConnectionTemplate: Connection;
  step: StepKind;
}
