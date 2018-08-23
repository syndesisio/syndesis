import { Integration, Step } from '@syndesis/ui/platform';
import {
  Component,
  Input,
  Output,
  EventEmitter,
  ViewEncapsulation
} from '@angular/core';

import { StepStore } from '@syndesis/ui/store';

@Component({
  encapsulation: ViewEncapsulation.None,
  selector: 'syndesis-integration-description',
  templateUrl: './integration-description.component.html',
  styleUrls: ['./integration-description.component.scss']
})
export class IntegrationDescriptionComponent {
  @Input() integration: Integration;
  @Input() stepStore: StepStore;

  @Output() viewDetails = new EventEmitter<Step>();
  @Output() attributeUpdated = new EventEmitter<{ [key: string]: string }>();

  onViewDetails(step: Step): void {
    this.viewDetails.emit(step);
  }

  onAttributeUpdated(attribute: string, value: string): void {
    this.attributeUpdated.emit({ [attribute]: value });
  }

  getStepLineClass(flowIndex: number, stepIndex: number): string {
    return stepIndex === this.integration.flows[flowIndex].steps.length - 1
      ? 'finish'
      : stepIndex === 0
        ? 'start'
        : '';
  }

}
