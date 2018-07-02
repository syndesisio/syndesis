import { Integration, Step } from '@syndesis/ui/platform';
import { Component, Input, Output, EventEmitter } from '@angular/core';

import { StepStore } from '@syndesis/ui/store';

@Component({
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

  getStepLineClass(index: number): string {
    return index === this.integration.steps.length - 1
      ? 'finish'
      : index === 0
        ? 'start'
        : '';
  }
}
