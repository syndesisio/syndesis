import { EventEmitter, Input, Output } from '@angular/core';

export abstract class EditableComponent<T> {
  @Input() value;
  @Input() placeholder = 'No value set';
  @Input() validationFn: (value) => string | Promise<string>;
  @Output() onSave = new EventEmitter<T>();
  editing: boolean;
  errorMessage: string;

  async submit(value) {
    this.errorMessage = await this.validate(value);
    if (!this.errorMessage) {
      this.save(value);
    }
  }

  validate(value): Promise<string> {
    const errorMessage = this.validationFn ? this.validationFn(value) : null;
    return Promise.resolve(errorMessage);
  }

  save(value) {
    this.value = value;
    this.onSave.emit(value);
    this.editing = false;
  }

  cancel() {
    this.errorMessage = null;
    this.editing = false;
  }
}
