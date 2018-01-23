import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Actions, Action } from '@syndesis/ui/model';
import { log, getCategory } from '@syndesis/ui/logging';

const category = getCategory('Actions');

@Component({
  selector: 'syndesis-list-actions',
  templateUrl: './list-actions.component.html',
  styleUrls: ['./list-actions.component.scss']
})
export class ListActionsComponent implements OnInit {
  truncateLimit = 80;
  truncateTrail = '…';
  selectedId = undefined;
  @Input() actions: Actions = [];
  @Input() loading: boolean;
  @Output() onSelected: EventEmitter<Action> = new EventEmitter();

  onSelect(action: Action) {
    log.debugc(() => 'Selected action (list): ' + action.name, category);
    this.selectedId = action.id;
    this.onSelected.emit(action);
  }

  isSelected(action: Action) {
    return action.id === this.selectedId;
  }

  toggled(open): void {
    log.debugc(() => 'Dropdown is now: ' + open);
  }

  ngOnInit() {
    log.debugc(
      () => 'Got actions: ' + JSON.stringify(this.actions, undefined, 2),
      category
    );
  }
}
