import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { log, getCategory } from '../logging';

import { Connection, Connections, Integration, Integrations } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';
import { IntegrationStore } from '../store/integration/integration.store';

const category = getCategory('Dashboard');

@Component({
  selector: 'ipaas-dashboard-empty-state',
  templateUrl: './emptystate.component.html',
  styleUrls: ['./emptystate.component.scss'],
})
export class EmptyStateComponent implements OnInit {
  connections: Observable<Connections>;
  @Input() loading: boolean;
  selectedId = undefined;
  truncateLimit = 80;
  truncateTrail = '…';

  constructor(private connectionStore: ConnectionStore) {
    this.connections = this.connectionStore.list;
  }

  onSelect(item) {
    log.debugc(() => 'Selected item (list): ' + item.name, category);
  }

  isSelected(item) {
    return item.id === this.selectedId;
  }

  ngOnInit() {
    this.connectionStore.loadAll();
  }
}
