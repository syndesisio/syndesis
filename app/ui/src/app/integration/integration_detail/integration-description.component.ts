import { Integration, Step } from '@syndesis/ui/platform';
import { NotificationService } from '@syndesis/ui/common';
import { CopyEvent, NotificationType } from 'patternfly-ng';
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

  constructor(private notificationService: NotificationService) {}

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

  handleCopy($event: CopyEvent, result: any): void {
    this.notify(result);
  }

  notify(result: any): void {
    this.notificationService.message(
      NotificationType.SUCCESS,
      null,
      result.msg,
      false,
      null,
      null);
  }
}
