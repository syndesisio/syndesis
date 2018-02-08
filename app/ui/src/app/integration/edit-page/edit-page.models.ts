import { Step } from '@syndesis/ui/platform';

export class FlowEvent {
  kind: string;
  [name: string]: any;
}

export interface IndexedStep {
  step: Step;
  index: number;
}
