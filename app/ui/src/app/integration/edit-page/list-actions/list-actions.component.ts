import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs';

import { Actions, Action } from '@syndesis/ui/platform';
import { log, getCategory } from '@syndesis/ui/logging';

const category = getCategory('Actions');

@Component({
  selector: 'syndesis-list-actions',
  templateUrl: './list-actions.component.html',
  styleUrls: ['./list-actions.component.scss']
})
export class ListActionsComponent {
  truncateLimit = 80;
  truncateTrail = '…';
  selectedId = undefined;
  @Input() actions: Actions = [];
  @Input() loading: boolean;
  @Output() onSelected: EventEmitter<Action> = new EventEmitter();

  onSelect(action: Action) {
    this.selectedId = action.id;
    this.onSelected.emit(action);
  }

  isSelected(action: Action) {
    return action.id === this.selectedId;
  }
}
