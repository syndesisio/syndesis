import { Step } from '@syndesis/ui/platform';

export class FlowEvent {
  kind: string;
  [name: string]: any;
}

export enum FlowErrorKind {
  NO_START_CONNECTION,
  NO_FINISH_CONNECTION,
  NO_NAME,
}

export class FlowError {
  kind: FlowErrorKind;
  [name: string]: any;
}

export interface IndexedStep {
  step: Step;
  index: number;
}
