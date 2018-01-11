import { Component, Pipe, PipeTransform } from '@angular/core';
import { StepStore, StepKind, StepKinds } from '@syndesis/ui/store';
import { CurrentFlow } from '@syndesis/ui/integrations';
import { Step, Steps, TypeFactory } from '@syndesis/ui/model';

export class StepVisibleConfig {
  position: number;
}

@Pipe({
  name: 'stepVisible',
  pure: false
})
export class StepVisiblePipe implements PipeTransform {
  constructor(private currentFlow: CurrentFlow) {}
  transform(objects: Array<StepKind>, config: StepVisibleConfig) {
    if (!this.currentFlow.loaded) {
      return false;
    }
    const position = config.position;
    const previous = this.currentFlow.getPreviousSteps(config.position);
    const subsequent = this.currentFlow.getSubsequentSteps(config.position);
    return objects.filter((s: StepKind) => {
      if (s.visible && typeof s.visible === 'function') {
        return s.visible(position, previous, subsequent);
      }
      return true;
    });
  }
}
