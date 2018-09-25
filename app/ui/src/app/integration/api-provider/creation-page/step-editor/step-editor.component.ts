import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ApiDefinition } from 'apicurio-design-studio';

@Component({
  selector: 'api-provider-creation-step-editor',
  templateUrl: './step-editor.component.html',
})
export class StepEditorComponent {
  @Input() title: string;
  @Input() specification: string;
  @Output() onBack = new EventEmitter<boolean>();
  @Output() onCancel = new EventEmitter<boolean>();
  @Output() onSave = new EventEmitter<string>();

  onApiDefinitionChange(apiDefinition: ApiDefinition) {
    this.onSave.emit(JSON.stringify(apiDefinition.spec));
  }
}
