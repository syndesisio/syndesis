import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
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

  @Input() integrations: Integrations;
  @Input() loading: boolean;
  selectedId = undefined;
  truncateLimit = 80;
  truncateTrail = '…';

  @Output() onSelected: EventEmitter<Integration> = new EventEmitter();

  onSelect(integration: Integration) {
    log.debugc(() => 'Selected integration (list): ' + integration.name, category);
    this.selectedId = integration.id;
    this.onSelected.emit(integration);
  }

  isSelected(integration: Integration) {
    return integration.id === this.selectedId;
  }

  ngOnInit() {
    log.debugc(() => 'Got integrations: ' + JSON.stringify(this.integrations, undefined, 2), category);
  }

}
