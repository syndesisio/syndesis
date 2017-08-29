import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import { FilterField } from 'patternfly-ng';

import { IntegrationStore } from '../../store/integration/integration.store';
import { Integrations } from '../../model';

@Component({
  selector: 'syndesis-integrations-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss'],
})
export class IntegrationsListPage implements OnInit {
  loading: Observable<boolean>;
  integrations: Observable<Integrations>;
  filteredIntegrations: Subject<Integrations> = new BehaviorSubject(<Integrations>{});
  filterFields: Array<FilterField> = [{
    id: 'currentStatus',
    title: 'State',
    type: 'select',
    placeholder: 'Filter by state...',
    queries: [
      { id: 'actived', value: 'Activated' },
      { id: 'deactivated', value: 'Deactivated' },
      { id: 'draft', value: 'Draft' },
    ],
  }];

  constructor(private store: IntegrationStore) {
    this.integrations = this.store.list;
    this.loading = this.store.loading;
  }

  ngOnInit() {
    this.store.loadAll();
  }
}
