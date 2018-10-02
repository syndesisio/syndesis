import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ActionReducerError } from '@syndesis/ui/platform';

@Component({
  selector: 'api-provider-creation-step-name',
  templateUrl: './step-name.component.html'
})
export class StepNameComponent {
  @Input() loading: boolean;
  @Input() name: string;
  @Input() description: string;
  @Input() creationError: ActionReducerError;
  @Output() onNameChange = new EventEmitter<string>();
  @Output() onDescriptionChange = new EventEmitter<string>();
  @Output() onSave = new EventEmitter<boolean>();
  @Output() onBack = new EventEmitter<boolean>();
}
