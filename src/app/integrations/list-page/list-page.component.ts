import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ObjectPropertyFilterConfig } from '../../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../../common/object-property-sort.pipe';
import { IntegrationStore } from '../../store/integration/integration.store';
import { Integrations } from '../../model';

@Component({
  selector: 'ipaas-integrations-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss'],
})
export class IntegrationsListPage implements OnInit {

  integrations: Observable<Integrations>;

  loading: Observable<boolean>;

  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name',
  };

  sort: ObjectPropertySortConfig = {
    sortField: 'name',
    descending: false,
  };

  constructor(private store: IntegrationStore) {
    this.integrations = this.store.list;
    this.loading = this.store.loading;
  }

  ngOnInit() {
    this.store.loadAll();
  }

}
