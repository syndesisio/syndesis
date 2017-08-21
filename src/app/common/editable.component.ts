import { Component, EventEmitter, OnInit, Input, Output } from '@angular/core';

@Component({
  selector: 'syndesis-editable',
  template: `
      <ng-container *ngIf="!editing">
        <ng-container *ngIf="!field || field === ''">
          <i>{{ placeholder }}</i>
        </ng-container>
        <ng-container *ngIf="field && field !== ''">
          {{ field }}
        </ng-container>
        <a (click)="startEditing()"><i class="fa fa-pencil"></i></a>
      </ng-container>
      <ng-container *ngIf="editing">
        <ng-container *ngIf="useTextArea">
          <textarea [(ngModel)]="field"></textarea>
          <p>
            <button class="btn btn-primary" (click)="stopEditing(true)">Save</button>
            <button class="btn btn-default" (click)="stopEditing(false)">Cancel</button>
          </p>
        </ng-container>
        <ng-container *ngIf="!useTextArea">
          <input type="text" [(ngModel)]="field">
          <button class="btn btn-primary" (click)="stopEditing(true)">Save</button>
          <button class="btn btn-default" (click)="stopEditing(false)">Cancel</button>
        </ng-container>
      </ng-container>
  `,
})
export class EditableComponent implements OnInit {
  @Input() placeholder = 'No value set';

  @Input() field = '';

  @Input() useTextArea: boolean;

  @Output() onSave = new EventEmitter<string>();
  editing = false;
  oldValue: string;

  constructor() {}

  startEditing() {
    const last = this.oldValue;
    if (last && last !== '') {
      this.oldValue = this.field;
      this.field = last;
    }
    this.editing = true;
  }

  stopEditing(save: boolean) {
    if (!save) {
      const last = this.field;
      if (this.oldValue) {
        this.field = this.oldValue;
      }
      this.oldValue = last;
    }
    this.editing = false;
    if (save) {
      this.onSave.emit(this.field);
    }
  }

  ngOnInit() {
    if (!this.placeholder || this.placeholder === '') {
      this.placeholder = 'No value set';
    }
  }
}
