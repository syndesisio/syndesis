import { Component, Input } from '@angular/core';
import { EditableComponent } from '@syndesis/ui/common/editable/editable.component';

@Component({
  selector: 'syndesis-editable-text',
  template: `
    <div class="syn-editable-text">
      <ng-template [ngIf]="!editing">
        <em class="text-muted" *ngIf="!value">
          {{ placeholder }}
        </em>
        <ng-container *ngIf="value">
          {{ value }}
        </ng-container>
        <button type="button" class="btn btn-link" (click)="editing = true">
          <i class="fa fa-pencil" aria-hidden="true" title="Click to edit"></i>
        </button>
      </ng-template>

      <ng-template [ngIf]="editing">
        <div class="form-group" [ngClass]="{'has-error': errorMessage}">
          <input #textInput type="text" class="form-control" [ngModel]="value" [name]="name">
          <span class="help-block" *ngIf="errorMessage">{{ errorMessage }}</span>
        </div>
        <button type="button" class="btn btn-primary" (click)="submit(textInput.value.trim())">Save</button>
        <button type="button" class="btn btn-default" (click)="cancel()">Cancel</button>
      </ng-template>
    </div>
  `,
  styles: [
    `
      .syn-editable-text {
        display: inline-flex;
        align-items: center;
      }
      .syn-editable-text .form-control {
        font-size: inherit;
        height: inherit;
        line-height: inherit;
      }
      :host-context(.toolbar-pf) .syn-editable-text .btn-link {
        padding: 2px 6px !important;
      }
      :host-context(.toolbar-pf) .syn-editable-text .form-group {
        padding-left: 0 !important;
      }
    `
  ]
})
export class EditableTextComponent extends EditableComponent<string> {
  @Input() name;
}
