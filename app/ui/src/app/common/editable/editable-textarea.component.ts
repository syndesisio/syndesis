import { Component } from '@angular/core';
import { EditableComponent } from './editable.component';

@Component({
  selector: 'syndesis-editable-textarea',
  template: `
    <div class="form-control-pf-editable form-control-pf-full-width"
      [ngClass]="{'form-control-pf-edit': editing, 'has-error': errorMessage}">
      <button
        (click)="startEditing(textareaInput)"
        type="button"
        class="form-control-pf-value">
        <em class="text-muted" *ngIf="!value">
          {{ placeholder }}
        </em>
        <ng-container *ngIf="value">
          {{ value }}
        </ng-container>
        <i class="glyphicon glyphicon-pencil" aria-hidden="true"></i>
      </button>
      <textarea #textareaInput
        [ngModel]="value"
        class="form-control form-control-pf-editor"
        autocomplete="off"
        aria-label="description"
        (keyup)="valueChanged($event)"></textarea>
      <span class="help-block pull-left" *ngIf="errorMessage">{{ errorMessage }}</span>
      <div class="action-buttons">
        <button
          type="button"
          class="btn btn-primary form-control-pf-save"
          aria-label="Save"
          [disabled]="!enableSave"
          (mousedown)="submit(textareaInput.value.trim())">
          <i class="glyphicon glyphicon-ok"></i>
        </button>
        <button
          type="button"
          class="btn btn-default form-control-pf-cancel"
          aria-label="Cancel"
          (click)="cancel()">
          <i class="glyphicon glyphicon-remove"></i>
        </button>
      </div>
    </div>
  `
})
export class EditableTextareaComponent extends EditableComponent {}
