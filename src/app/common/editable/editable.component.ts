import { Component, EventEmitter, Input, Output } from '@angular/core';

export abstract class EditableComponent {

  @Input() placeholder = 'No value set';
  @Input() value;
  @Output() onSave = new EventEmitter<any>();
  tempValue = null;

  startEditing() {
    this.tempValue = this.value;
  }

  save() {
    this.value = this.tempValue;
    this.tempValue = null;
    this.onSave.emit(this.value);
  }

  cancel() {
    this.tempValue = null;
  }

  get editing() {
    return this.tempValue !== null;
  }

}
