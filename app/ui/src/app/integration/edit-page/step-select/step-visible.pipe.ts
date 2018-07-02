import { Component, Pipe, PipeTransform } from '@angular/core';
import { StepStore, StepKind, StepKinds } from '@syndesis/ui/store';
import { CurrentFlowService } from '@syndesis/ui/integration/edit-page';
import { Step, Steps } from '@syndesis/ui/platform';

export class StepVisibleConfig {
  position: number;
}

@Pipe({
  name: 'stepVisible',
  pure: false
})
export class StepVisiblePipe implements PipeTransform {
  constructor(private currentFlowService: CurrentFlowService) {}
  transform(objects: Array<StepKind>, config: StepVisibleConfig) {
    if (!this.currentFlowService.loaded) {
      return false;
    }
    const position = config.position;
    const previous = this.currentFlowService.getPreviousSteps(config.position);
    const subsequent = this.currentFlowService.getSubsequentSteps(
      config.position
    );
    return objects.filter((s: StepKind) => {
      if (s.visible && typeof s.visible === 'function') {
        return s.visible(position, previous, subsequent);
      }
      return true;
    });
  }
}
