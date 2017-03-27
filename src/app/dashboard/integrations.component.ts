import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { log, getCategory } from '../logging';

import { Integration, Integrations } from '../model';
import { IntegrationStore } from '../store/integration/integration.store';

const category = getCategory('Dashboard');

@Component({
  selector: 'ipaas-dashboard-integrations',
  templateUrl: './integrations.component.html',
  styleUrls: [],
})
export class DashboardIntegrationsComponent implements OnInit {

  integrations: Observable<Integrations>;
  loading: Observable<boolean>;
  selectedId = undefined;
  truncateLimit = 80;
  truncateTrail = '…';

  constructor(private store: IntegrationStore) {
    this.integrations = this.store.list;
    this.loading = this.store.loading;
  }

  onSelect(integration: Integration) {
    log.debugc(() => 'Selected integration (list): ' + integration.name, category);
  }

  isSelected(integration: Integration) {
    return integration.id === this.selectedId;
  }

  ngOnInit() {
    this.store.loadAll();
  }


}
