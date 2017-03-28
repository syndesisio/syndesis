import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { log, getCategory } from '../logging';

import { Connection, Connections, Integration, Integrations } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';
import { IntegrationStore } from '../store/integration/integration.store';

const category = getCategory('Dashboard');

@Component({
  selector: 'ipaas-dashboard-integrations',
  templateUrl: './integrations.component.html',
  styleUrls: ['./integrations.component.scss'],
})
export class DashboardIntegrationsComponent implements OnInit {

  connections: Observable<Connections>;
  @Input() integrations: Integrations;
  @Input() loading: boolean;
  selectedId = undefined;
  truncateLimit = 80;
  truncateTrail = '…';

  constructor(private connectionStore: ConnectionStore) {
    this.connections = this.connectionStore.list;
  }


  //-----  Selecting an Integration ------------------->>

  @Output() onSelected: EventEmitter<Integration> = new EventEmitter();

  onSelect(integration: Integration) {
    log.debugc(() => 'Selected integration (list): ' + integration.name, category);
    this.selectedId = integration.id;
    this.onSelected.emit(integration);
  }

  isSelected(integration: Integration) {
    return integration.id === this.selectedId;
  }

  //-----  Icons ------------------->>

  getStartIcon(integration: Integration) {
    const connection = integration.steps[0].connection;
    const icon = 'fa fa-plane';

    return (connection || {})['icon'] || 'fa-plane';
  }

  getFinishIcon(integration: Integration) {
    const connection = integration.steps[integration.steps.length - 1].connection;
    return (connection || {})['icon'] || 'fa-plane';
  }

  //-----  Initialization ------------------->>

  ngOnInit() {
    log.debugc(() => 'Got integrations: ' + JSON.stringify(this.integrations, undefined, 2), category);
    this.connectionStore.loadAll();
  }

}
