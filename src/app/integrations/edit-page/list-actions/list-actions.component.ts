import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ToasterService } from 'angular2-toaster';

import { Actions, Action } from '../../../model';
import { log, getCategory } from '../../../logging';

const category = getCategory('Actions');

@Component({
  selector: 'ipaas-list-actions',
  templateUrl: './list-actions.component.html',
  styleUrls: ['./list.component.scss'],
})
export class ListActionsComponent implements OnInit {

  truncateLimit = 80;
  truncateTrail = '…';
  selectedId = undefined;
  private toasterService: ToasterService;
  @Input() actions: Actions;
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
    log.debugc(() => 'Got actions: ' + JSON.stringify(this.actions, undefined, 2), category);
  }
}
