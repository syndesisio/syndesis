import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ToasterService } from 'angular2-toaster';

import { Actions, Action } from '../../../model';
import { log, getCategory } from '../../../logging';
import { ObjectPropertyFilterConfig } from '../../../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../../../common/object-property-sort.pipe';

const category = getCategory('Actions');

@Component({
  selector: 'syndesis-list-actions',
  templateUrl: './list-actions.component.html',
  styleUrls: ['./list-actions.component.scss'],
})
export class ListActionsComponent implements OnInit {
  truncateLimit = 80;
  truncateTrail = '…';
  selectedId = undefined;
  private toasterService: ToasterService;
  @Input() actions: Actions;
  @Input() loading: boolean;
  @Output() onSelected: EventEmitter<Action> = new EventEmitter();
  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name',
  };

  sort: ObjectPropertySortConfig = {
    sortField: 'name',
    descending: false,
  };

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
      category,
    );
  }
}
