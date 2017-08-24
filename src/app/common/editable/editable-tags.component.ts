import { Component } from '@angular/core';
import { EditableComponent } from './editable.component';

@Component({
  selector: 'syndesis-editable-tags',
  template: `
    <ng-container *ngIf="!editing">
      <em class="text-muted" *ngIf="!value || value.length === 0">
        {{ placeholder }}
      </em>
      <ng-container *ngIf="value && value.length !== 0">
        <ng-container *ngFor="let tag of value">
          <span class="label label-primary">{{tag}}</span>
        </ng-container>
      </ng-container>
      <button type="button" class="btn btn-link" (click)="startEditing()">
        <i class="fa fa-pencil" aria-hidden="true"></i>
      </button>
    </ng-container>
    <ng-container *ngIf="editing">
      <div class="form-group">
        <tag-input [(ngModel)]="tempValue"
                   name="tags"
                   data-id="tags"
                   theme="bootstrap"
                   [editable]="true"
                   [modelAsStrings]="true"
                   [ripple]="false"
                   [inputId]="tagsInput"></tag-input>
      </div>
      <button class="btn btn-primary" (click)="save()">Save</button>
      <button class="btn btn-default" (click)="cancel()">Cancel</button>
    </ng-container>
  `,
})
export class EditableTagsComponent extends EditableComponent {
}
