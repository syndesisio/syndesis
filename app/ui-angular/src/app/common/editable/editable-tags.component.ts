import { Component } from '@angular/core';
import { EditableComponent } from '@syndesis/ui/common/editable/editable.component';

@Component({
  selector: 'syndesis-editable-tags',
  template: `
    <ng-template [ngIf]="!editing">
      <em class="text-muted" *ngIf="!value || value.length === 0">
        {{ placeholder }}
      </em>
      <ng-container *ngIf="value && value.length !== 0">
        <ng-container *ngFor="let tag of value">
          <span class="label label-primary">{{ tag }}</span>
        </ng-container>
      </ng-container>
      <button type="button" class="btn btn-link" (click)="editing = true">
        <i class="fa fa-pencil" aria-hidden="true" title="Click to edit"></i>
      </button>
    </ng-template>

    <ng-template [ngIf]="editing">
      <div class="form-group">
        <tag-input #tagInput
                   [ngModel]="value"
                   name="tags"
                   data-id="tags"
                   theme="bootstrap"
                   [editable]="true"
                   [modelAsStrings]="true"
                   [ripple]="false"
                   [separatorKeyCodes]="[188]"
                   [addOnBlur]="true"></tag-input>
      </div>
      <button type="button" class="btn btn-primary" (click)="submit(tagInput.items)">Save</button>
      <button type="button" class="btn btn-default" (click)="cancel()">Cancel</button>
    </ng-template>
  `
})
export class EditableTagsComponent extends EditableComponent<string> {}
