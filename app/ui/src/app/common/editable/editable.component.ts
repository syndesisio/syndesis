import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgModel } from '@angular/forms';

export abstract class EditableComponent {
  @Input() value;
  @Input() placeholder = 'No value set';
  @Input() validationFn: (value) => string | Promise<string>;
  @Output() onSave = new EventEmitter<any>();
  editing: boolean;
  errorMessage: string;
  originalValue: string;
  enableSave: boolean;

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

  cancel(el) {
    this.errorMessage = null;
    this.editing = false;
    if (!el) {
      // currently for the input/tag controls el won't be set since the template doesn't pass it in as an argument
      return;
    }
    // 'el' here is an instance of NgModel ideally
    if (el.reset) {
      el.reset(this.originalValue);
    }
  }

  startEditing(el) {
    // Access the underlying control, for the textarea template currently an NgModel instance is being passed in
    const _el = el.control || el;
    this.enableSave = false;
    this.editing = true;
    this.originalValue = el.value;
    if (_el.focus) {
      setTimeout(() => _el.focus(), 500);
    }
  }

  valueChanged(e) {
    // ideally we should get ahold of the NgModel instance, no need to access the DOM element
    if (this.originalValue === e.srcElement.value) {
      this.enableSave = false;
    } else {
      this.enableSave = true;
    }
  }
}
