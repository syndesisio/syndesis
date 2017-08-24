import { Component } from '@angular/core';
import { EditableComponent } from './editable.component';

@Component({
  selector: 'syndesis-editable-textarea',
  template: `
    <ng-container *ngIf="!editing">
      <em class="text-muted" *ngIf="!value">
        {{ placeholder }}
      </em>
      <ng-container *ngIf="value">
        {{ value }}
      </ng-container>
      <button type="button" class="btn btn-link" (click)="startEditing()">
        <i class="fa fa-pencil" aria-hidden="true"></i>
      </button>
    </ng-container>
    <ng-container *ngIf="editing">
      <div class="form-group">
        <textarea class="form-control" [(ngModel)]="tempValue"></textarea>
      </div>
      <button class="btn btn-primary" (click)="save()">Save</button>
      <button class="btn btn-default" (click)="cancel()">Cancel</button>
    </ng-container>
  `,
})
export class EditableTextareaComponent extends EditableComponent {
}
